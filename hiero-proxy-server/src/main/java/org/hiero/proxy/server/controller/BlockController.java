package org.hiero.proxy.server.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.hiero.base.mirrornode.BlockRepository;
import org.hiero.proxy.server.dto.response.BlockResponse;
import org.hiero.proxy.server.exception.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/blocks")
@Tag(
        name = "Blocks",
        description = "Read-only operations for querying Hiero network blocks from the mirror node. "
                + "Blocks can be looked up by their sequential number or by their EVM-compatible hash.")
public class BlockController {

    private final BlockRepository blockRepository;

    public BlockController(BlockRepository blockRepository) {
        this.blockRepository = Objects.requireNonNull(blockRepository, "blockRepository must not be null");
    }

    // List

    @GetMapping
    @Operation(
            summary = "List blocks",
            description = "Fetches the first page of blocks from the Hiero mirror node in descending order "
                    + "(most recent first). Each block contains transaction count, gas usage, "
                    + "EVM hash, HAPI version, and timestamp range.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Blocks retrieved successfully.",
                    content = @Content(
                            schema = @Schema(implementation = BlockResponse.class, type = "array"),
                            examples = @ExampleObject(value = """
                                    [
                                      {
                                        "count": 3,
                                        "hapiVersion": "0.49.0",
                                        "hash": "0x5b6e7a8c...",
                                        "name": "2024-01-15T10:30:00Z",
                                        "number": 61234567,
                                        "previousHash": "0x4a5d6b7c...",
                                        "size": 2048,
                                        "fromTimestampEpochSecond": 1700000000,
                                        "toTimestampEpochSecond": 1700000002,
                                        "gasUsed": 850000,
                                        "logsBloom": null
                                      }
                                    ]"""))),
            @ApiResponse(
                    responseCode = "500",
                    description = "Could not retrieve blocks.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<List<BlockResponse>> listBlocks() throws Exception {
        return ResponseEntity.ok(
                blockRepository.findAll()
                        .getData()
                        .stream()
                        .map(BlockResponse::from)
                        .collect(Collectors.toList())
        );
    }

    // Get by Number

    @GetMapping("/number/{number}")
    @Operation(
            summary = "Get block by number",
            description = "Fetches a specific block by its sequential block number from the Hiero mirror node.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Block retrieved successfully.",
                    content = @Content(
                            schema = @Schema(implementation = BlockResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "count": 3,
                                      "hapiVersion": "0.49.0",
                                      "hash": "0x5b6e7a8c...",
                                      "name": "2024-01-15T10:30:00Z",
                                      "number": 61234567,
                                      "previousHash": "0x4a5d6b7c...",
                                      "size": 2048,
                                      "fromTimestampEpochSecond": 1700000000,
                                      "toTimestampEpochSecond": 1700000002,
                                      "gasUsed": 850000,
                                      "logsBloom": null
                                    }"""))),
            @ApiResponse(
                    responseCode = "400",
                    description = "Block number must not be negative.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(
                    responseCode = "404",
                    description = "No block found with the given number.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(
                    responseCode = "500",
                    description = "Could not retrieve block.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<BlockResponse> getBlockByNumber(
            @Parameter(description = "The sequential block number.", required = true, example = "61234567")
            @PathVariable("number") long number) throws Exception {

        if (number < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Block number must not be negative");
        }

        return blockRepository.findByNumber(number)
                .map(BlockResponse::from)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Block not found: number=" + number));
    }

    // Get by Hash

    @GetMapping("/hash/{hash}")
    @Operation(
            summary = "Get block by hash",
            description = "Fetches a specific block by its EVM-compatible 32-byte hash from the Hiero mirror node. "
                    + "The hash should be provided as a hex-encoded string (with or without the `0x` prefix).")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Block retrieved successfully.",
                    content = @Content(
                            schema = @Schema(implementation = BlockResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "count": 3,
                                      "hapiVersion": "0.49.0",
                                      "hash": "0x5b6e7a8c...",
                                      "name": "2024-01-15T10:30:00Z",
                                      "number": 61234567,
                                      "previousHash": "0x4a5d6b7c...",
                                      "size": 2048,
                                      "fromTimestampEpochSecond": 1700000000,
                                      "toTimestampEpochSecond": 1700000002,
                                      "gasUsed": 850000,
                                      "logsBloom": null
                                    }"""))),
            @ApiResponse(
                    responseCode = "404",
                    description = "No block found with the given hash.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(
                    responseCode = "500",
                    description = "Could not retrieve block.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<BlockResponse> getBlockByHash(
            @Parameter(
                    description = "The EVM-compatible block hash (hex-encoded, with or without 0x prefix).",
                    required = true,
                    example = "0x5b6e7a8c9d0e1f2a3b4c5d6e7f8a9b0c1d2e3f4a5b6c7d8e9f0a1b2c3d4e5f6a")
            @PathVariable("hash") String hash) throws Exception {

        return blockRepository.findByHash(hash)
                .map(BlockResponse::from)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Block not found: hash=" + hash));
    }
}
