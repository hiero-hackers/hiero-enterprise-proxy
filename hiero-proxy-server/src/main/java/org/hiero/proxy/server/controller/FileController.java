package org.hiero.proxy.server.controller;

import com.hedera.hashgraph.sdk.FileId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.hiero.base.FileClient;
import org.hiero.proxy.server.dto.request.CreateFileRequest;
import org.hiero.proxy.server.dto.request.UpdateFileExpirationRequest;
import org.hiero.proxy.server.dto.request.UpdateFileRequest;
import org.hiero.proxy.server.dto.response.FileContentsResponse;
import org.hiero.proxy.server.dto.response.FileCreatedResponse;
import org.hiero.proxy.server.dto.response.FileInfoResponse;
import org.hiero.proxy.server.dto.response.SuccessResponse;
import org.hiero.proxy.server.exception.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Base64;
import java.util.Objects;

@RestController
@RequestMapping("/api/v1/files")
@Tag(
        name = "Files",
        description = "Operations for managing files on the Hiero File Service (HFS). "
                + "HFS files can store arbitrary byte content on the ledger — "
                + "smart contract bytecode, metadata, configuration, and more. "
                + "All content is exchanged as Base64-encoded strings.")
public class FileController {

    private final FileClient fileClient;

    public FileController(FileClient fileClient) {
        this.fileClient = Objects.requireNonNull(fileClient, "fileClient must not be null");
    }

    // Create

    @PostMapping
    @Operation(
            summary = "Create a file",
            description = "Creates a new file on the Hiero File Service (HFS) with the provided content. "
                    + "Content must be Base64-encoded. "
                    + "The operator account pays the transaction fee and becomes the file owner. "
                    + "Optionally provide an `expirationTimeEpochSecond` to set when the file auto-deletes; "
                    + "if omitted, the network default expiry (~90 days) is used. "
                    + "Large files (> 4 KB) are automatically split across multiple transactions.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "File created successfully.",
                    content = @Content(
                            schema = @Schema(implementation = FileCreatedResponse.class),
                            examples = @ExampleObject(value = """
                                    { "fileId": "0.0.77001" }"""))),
            @ApiResponse(
                    responseCode = "400",
                    description = "Contents are missing, blank, or not valid Base64.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(
                    responseCode = "500",
                    description = "File could not be created on the network.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<FileCreatedResponse> createFile(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Base64-encoded file content and optional expiration time.",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = CreateFileRequest.class),
                            examples = @ExampleObject(value = """
                                    { "contents": "SGVsbG8gSGllcm8h" }""")))
            @RequestBody CreateFileRequest request) throws Exception {

        if (request.contents() == null || request.contents().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File contents must not be blank");
        }

        final byte[] bytes;
        try {
            bytes = Base64.getDecoder().decode(request.contents());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Contents must be a valid Base64-encoded string");
        }

        final FileId fileId;
        if (request.expirationTimeEpochSecond() != null) {
            fileId = fileClient.createFile(bytes, Instant.ofEpochSecond(request.expirationTimeEpochSecond()));
        } else {
            fileId = fileClient.createFile(bytes);
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(FileCreatedResponse.of(fileId));
    }

    // Read Contents

    @GetMapping("/{fileId}/contents")
    @Operation(
            summary = "Read file contents",
            description = "Reads and returns the raw contents of an HFS file as a Base64-encoded string.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "File contents retrieved successfully.",
                    content = @Content(
                            schema = @Schema(implementation = FileContentsResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "fileId": "0.0.77001",
                                      "contents": "SGVsbG8gSGllcm8h"
                                    }"""))),
            @ApiResponse(
                    responseCode = "500",
                    description = "Could not read file contents.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<FileContentsResponse> readFile(
            @Parameter(description = "The Hedera file ID.", required = true, example = "0.0.77001")
            @PathVariable("fileId") String fileId) throws Exception {

        byte[] contents = fileClient.readFile(fileId);
        return ResponseEntity.ok(FileContentsResponse.of(fileId, contents));
    }

    // Read Info

    @GetMapping("/{fileId}")
    @Operation(
            summary = "Get file info",
            description = "Returns metadata for an HFS file: size in bytes, deletion status, "
                    + "and expiration time. Does not return the file contents.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "File info retrieved successfully.",
                    content = @Content(
                            schema = @Schema(implementation = FileInfoResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "fileId": "0.0.77001",
                                      "size": 16,
                                      "deleted": false,
                                      "expirationTimeEpochSecond": 1800000000
                                    }"""))),
            @ApiResponse(
                    responseCode = "500",
                    description = "Could not retrieve file info.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<FileInfoResponse> getFileInfo(
            @Parameter(description = "The Hedera file ID.", required = true, example = "0.0.77001")
            @PathVariable("fileId") String fileId) throws Exception {

        FileId fid = FileId.fromString(fileId);
        int size           = fileClient.getSize(fid);
        boolean deleted    = fileClient.isDeleted(fid);
        long expiry        = fileClient.getExpirationTime(fid).getEpochSecond();

        return ResponseEntity.ok(FileInfoResponse.of(fileId, size, deleted, expiry));
    }

    // Update Contents

    @PutMapping("/{fileId}/contents")
    @Operation(
            summary = "Update file contents",
            description = "Fully replaces the contents of an existing HFS file with new Base64-encoded content. "
                    + "Large content (> 4 KB) is automatically split across multiple append transactions.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "File contents updated successfully.",
                    content = @Content(
                            schema = @Schema(implementation = SuccessResponse.class),
                            examples = @ExampleObject(value = """
                                    { "message": "File 0.0.77001 contents updated successfully." }"""))),
            @ApiResponse(
                    responseCode = "400",
                    description = "Contents are missing, blank, or not valid Base64.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(
                    responseCode = "500",
                    description = "File could not be updated.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<SuccessResponse> updateFileContents(
            @Parameter(description = "The Hedera file ID.", required = true, example = "0.0.77001")
            @PathVariable("fileId") String fileId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "New Base64-encoded file content.",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = UpdateFileRequest.class),
                            examples = @ExampleObject(value = """
                                    { "contents": "VXBkYXRlZCBjb250ZW50" }""")))
            @RequestBody UpdateFileRequest request) throws Exception {

        if (request.contents() == null || request.contents().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File contents must not be blank");
        }

        final byte[] bytes;
        try {
            bytes = Base64.getDecoder().decode(request.contents());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Contents must be a valid Base64-encoded string");
        }

        fileClient.updateFile(FileId.fromString(fileId), bytes);
        return ResponseEntity.ok(SuccessResponse.of("File " + fileId + " contents updated successfully."));
    }

    // Update Expiration

    @PutMapping("/{fileId}/expiration")
    @Operation(
            summary = "Update file expiration time",
            description = "Sets a new expiration time for an existing HFS file. "
                    + "The new time must be in the future. "
                    + "When a file expires it is automatically deleted from the network.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "File expiration time updated successfully.",
                    content = @Content(
                            schema = @Schema(implementation = SuccessResponse.class),
                            examples = @ExampleObject(value = """
                                    { "message": "File 0.0.77001 expiration time updated successfully." }"""))),
            @ApiResponse(
                    responseCode = "400",
                    description = "Expiration time is in the past.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(
                    responseCode = "500",
                    description = "File expiration time could not be updated.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<SuccessResponse> updateFileExpiration(
            @Parameter(description = "The Hedera file ID.", required = true, example = "0.0.77001")
            @PathVariable("fileId") String fileId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "New expiration time as a Unix epoch second.",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = UpdateFileExpirationRequest.class),
                            examples = @ExampleObject(value = """
                                    { "expirationTimeEpochSecond": 1900000000 }""")))
            @RequestBody UpdateFileExpirationRequest request) throws Exception {

        Instant expiry = Instant.ofEpochSecond(request.expirationTimeEpochSecond());
        if (expiry.isBefore(Instant.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Expiration time must be in the future");
        }

        fileClient.updateExpirationTime(FileId.fromString(fileId), expiry);
        return ResponseEntity.ok(SuccessResponse.of("File " + fileId + " expiration time updated successfully."));
    }

    // Delete

    @DeleteMapping("/{fileId}")
    @Operation(
            summary = "Delete a file",
            description = "Permanently deletes an HFS file from the Hiero network. "
                    + "The operator account must be the file owner to authorise this operation.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "File deleted successfully.",
                    content = @Content(
                            schema = @Schema(implementation = SuccessResponse.class),
                            examples = @ExampleObject(value = """
                                    { "message": "File 0.0.77001 deleted successfully." }"""))),
            @ApiResponse(
                    responseCode = "500",
                    description = "File could not be deleted.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<SuccessResponse> deleteFile(
            @Parameter(description = "The Hedera file ID.", required = true, example = "0.0.77001")
            @PathVariable("fileId") String fileId) throws Exception {

        fileClient.deleteFile(fileId);
        return ResponseEntity.ok(SuccessResponse.of("File " + fileId + " deleted successfully."));
    }
}
