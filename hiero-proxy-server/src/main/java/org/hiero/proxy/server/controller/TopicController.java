package org.hiero.proxy.server.controller;

import com.hedera.hashgraph.sdk.PrivateKey;
import com.hedera.hashgraph.sdk.TopicId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.hiero.base.TopicClient;
import org.hiero.base.mirrornode.TopicRepository;
import org.hiero.proxy.server.dto.request.CreatePrivateTopicRequest;
import org.hiero.proxy.server.dto.request.CreatePrivateTopicWithAdminKeyRequest;
import org.hiero.proxy.server.dto.request.CreateTopicRequest;
import org.hiero.proxy.server.dto.request.CreateTopicWithAdminKeyRequest;
import org.hiero.proxy.server.dto.request.DeleteTopicRequest;
import org.hiero.proxy.server.dto.request.SubmitBinaryMessageRequest;
import org.hiero.proxy.server.dto.request.SubmitMessageRequest;
import org.hiero.proxy.server.dto.request.UpdateTopicKeyRequest;
import org.hiero.proxy.server.dto.request.UpdateTopicMemoRequest;
import org.hiero.proxy.server.dto.request.UpdateTopicRequest;
import org.hiero.proxy.server.dto.response.SuccessResponse;
import org.hiero.proxy.server.dto.response.TopicCreatedResponse;
import org.hiero.proxy.server.dto.response.TopicKeyRotationResponse;
import org.hiero.proxy.server.dto.response.TopicMessageResponse;
import org.hiero.proxy.server.dto.response.TopicResponse;
import org.hiero.proxy.server.dto.response.TopicUpdatedResponse;
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

import java.util.Base64;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/v1/topics")
@Tag(name = "Topics", description = "Operations for managing topics on a Hiero network")
public class TopicController {

    private final TopicClient topicClient;
    private final TopicRepository topicRepository;

    public TopicController(TopicClient topicClient, TopicRepository topicRepository) {
        this.topicClient = Objects.requireNonNull(topicClient, "topicClient must not be null");
        this.topicRepository = Objects.requireNonNull(topicRepository, "topicRepository must not be null");
    }

    // Topic creation

    @PostMapping
    @Operation(
            summary = "Create a public topic",
            description = "Creates a new public topic on the Hiero network. "
                    + "The operator account is used as the admin key. "
                    + "An optional memo can be provided in the request body; omit the body to create a topic with no memo.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Topic created successfully.",
                    content = @Content(
                            schema = @Schema(implementation = TopicCreatedResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "topicId": "0.0.55001",
                                      "submitPrivateKey": null,
                                      "submitPublicKey": null
                                    }"""))),
            @ApiResponse(
                    responseCode = "500",
                    description = "Topic could not be created on the network.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<TopicCreatedResponse> createTopic(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Optional memo for the topic. Omit body or leave memo null to create with no memo.",
                    required = false,
                    content = @Content(
                            schema = @Schema(implementation = CreateTopicRequest.class),
                            examples = @ExampleObject(value = """
                                    { "memo": "Enterprise event bus topic" }""")))
            @RequestBody(required = false) CreateTopicRequest request) throws Exception {
        TopicId topicId = (request != null && request.memo() != null && !request.memo().isBlank())
                ? topicClient.createTopic(request.memo())
                : topicClient.createTopic();
        return ResponseEntity.status(HttpStatus.CREATED).body(TopicCreatedResponse.of(topicId));
    }

    @PostMapping("/private")
    @Operation(
            summary = "Create a private topic",
            description = "Creates a new private topic on the Hiero network. "
                    + "The server generates a fresh ED25519 submit key and returns it in the response — save it securely. "
                    + "The operator account is used as the admin key. "
                    + "Messages can only be submitted using the returned submit key.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Private topic created successfully. The submit key pair is included in the response.",
                    content = @Content(
                            schema = @Schema(implementation = TopicCreatedResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "topicId": "0.0.55002",
                                      "submitPrivateKey": "302e020100300506032b657004220420ddeeff...",
                                      "submitPublicKey": "302a300506032b6570032100ddeeff..."
                                    }"""))),
            @ApiResponse(
                    responseCode = "500",
                    description = "Topic could not be created on the network.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<TopicCreatedResponse> createPrivateTopic(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Optional memo for the private topic.",
                    required = false,
                    content = @Content(
                            schema = @Schema(implementation = CreatePrivateTopicRequest.class),
                            examples = @ExampleObject(value = """
                                    { "memo": "Private enterprise channel" }""")))
            @RequestBody(required = false) CreatePrivateTopicRequest request) throws Exception {
        PrivateKey newSubmitKey = PrivateKey.generateED25519();
        TopicId topicId = (request != null && request.memo() != null && !request.memo().isBlank())
                ? topicClient.createPrivateTopic(newSubmitKey, request.memo())
                : topicClient.createPrivateTopic(newSubmitKey);
        return ResponseEntity.status(HttpStatus.CREATED).body(TopicCreatedResponse.of(topicId, newSubmitKey));
    }

    @PostMapping("/with-admin-key")
    @Operation(
            summary = "Create a public topic with a custom admin key",
            description = "Creates a new public topic on the Hiero network using the provided private key as the admin key. "
                    + "Use this when you want to control the topic with a key other than the operator key.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Topic created successfully.",
                    content = @Content(
                            schema = @Schema(implementation = TopicCreatedResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "topicId": "0.0.55003",
                                      "submitPrivateKey": null,
                                      "submitPublicKey": null
                                    }"""))),
            @ApiResponse(
                    responseCode = "500",
                    description = "Topic could not be created on the network.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<TopicCreatedResponse> createTopicWithAdminKey(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Custom admin key and optional memo.",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = CreateTopicWithAdminKeyRequest.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "adminPrivateKey": "302e020100300506032b657004220420aabbcc...",
                                      "memo": "Enterprise audit log topic"
                                    }""")))
            @RequestBody CreateTopicWithAdminKeyRequest request) throws Exception {
        PrivateKey adminKey = PrivateKey.fromString(request.adminPrivateKey());
        TopicId topicId = (request.memo() != null && !request.memo().isBlank())
                ? topicClient.createTopic(adminKey, request.memo())
                : topicClient.createTopic(adminKey);
        return ResponseEntity.status(HttpStatus.CREATED).body(TopicCreatedResponse.of(topicId));
    }

    @PostMapping("/private/with-admin-key")
    @Operation(
            summary = "Create a private topic with a custom admin key",
            description = "Creates a new private topic using the provided private key as the admin key. "
                    + "The server generates a fresh ED25519 submit key and returns it in the response — save it securely.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Private topic created successfully. The submit key pair is included in the response.",
                    content = @Content(
                            schema = @Schema(implementation = TopicCreatedResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "topicId": "0.0.55004",
                                      "submitPrivateKey": "302e020100300506032b657004220420ddeeff...",
                                      "submitPublicKey": "302a300506032b6570032100ddeeff..."
                                    }"""))),
            @ApiResponse(
                    responseCode = "500",
                    description = "Topic could not be created on the network.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<TopicCreatedResponse> createPrivateTopicWithAdminKey(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Custom admin key and optional memo. Server generates the submit key.",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = CreatePrivateTopicWithAdminKeyRequest.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "adminPrivateKey": "302e020100300506032b657004220420aabbcc...",
                                      "memo": "Private enterprise channel"
                                    }""")))
            @RequestBody CreatePrivateTopicWithAdminKeyRequest request) throws Exception {
        PrivateKey adminKey = PrivateKey.fromString(request.adminPrivateKey());
        PrivateKey newSubmitKey = PrivateKey.generateED25519();
        TopicId topicId = (request.memo() != null && !request.memo().isBlank())
                ? topicClient.createPrivateTopic(adminKey, newSubmitKey, request.memo())
                : topicClient.createPrivateTopic(adminKey, newSubmitKey);
        return ResponseEntity.status(HttpStatus.CREATED).body(TopicCreatedResponse.of(topicId, newSubmitKey));
    }

    // Topic queries

    @GetMapping("/{topicId}")
    @Operation(
            summary = "Get topic info",
            description = "Fetches detailed topic information from the Hiero mirror node, including "
                    + "memo, deletion status, creation timestamp, auto-renew period, and whether admin/submit keys are configured.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Topic info retrieved successfully.",
                    content = @Content(
                            schema = @Schema(implementation = TopicResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "topicId": "0.0.55001",
                                      "memo": "Enterprise event bus topic",
                                      "deleted": false,
                                      "createdTimestamp": "2024-01-15T10:30:00Z",
                                      "autoRenewPeriod": 7776000,
                                      "hasAdminKey": true,
                                      "hasSubmitKey": false
                                    }"""))),
            @ApiResponse(
                    responseCode = "404",
                    description = "Topic not found on the mirror node.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(
                    responseCode = "500",
                    description = "Could not retrieve topic info.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<TopicResponse> getTopicInfo(
            @Parameter(description = "The Hiero topic ID.", required = true, example = "0.0.55001")
            @PathVariable("topicId") String topicId) throws Exception {
        return topicRepository.findTopicById(topicId)
                .map(TopicResponse::from)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Topic not found: " + topicId));
    }

    // Memo update

    @PutMapping("/{topicId}/memo")
    @Operation(
            summary = "Update topic memo",
            description = "Updates the memo field of the specified topic. "
                    + "The topic's admin private key is required to authorise the transaction.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Memo updated successfully.",
                    content = @Content(
                            schema = @Schema(implementation = SuccessResponse.class),
                            examples = @ExampleObject(value = """
                                    { "message": "Topic memo updated successfully." }"""))),
            @ApiResponse(
                    responseCode = "500",
                    description = "Memo could not be updated.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<SuccessResponse> updateTopicMemo(
            @Parameter(description = "The Hiero topic ID.", required = true, example = "0.0.55001")
            @PathVariable("topicId") String topicId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Admin private key and new memo value.",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = UpdateTopicMemoRequest.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "adminPrivateKey": "302e020100300506032b657004220420aabbcc...",
                                      "memo": "Updated enterprise event bus topic"
                                    }""")))
            @RequestBody UpdateTopicMemoRequest request) throws Exception {
        topicClient.updateTopic(
                TopicId.fromString(topicId),
                PrivateKey.fromString(request.adminPrivateKey()),
                request.memo());
        return ResponseEntity.ok(SuccessResponse.of("Topic memo updated successfully."));
    }

    // Admin key rotation

    @PutMapping("/{topicId}/admin-key")
    @Operation(
            summary = "Rotate topic admin key",
            description = "Replaces the admin key of the specified topic with a newly generated ED25519 key pair. "
                    + "The current admin private key is required to authorise the rotation. "
                    + "The new key pair is generated server-side and returned — save it securely.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Admin key rotated successfully. Returns the new key pair.",
                    content = @Content(
                            schema = @Schema(implementation = TopicKeyRotationResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "topicId": "0.0.55001",
                                      "newPrivateKey": "302e020100300506032b657004220420ddeeff...",
                                      "newPublicKey": "302a300506032b6570032100ddeeff..."
                                    }"""))),
            @ApiResponse(
                    responseCode = "500",
                    description = "Admin key could not be rotated.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<TopicKeyRotationResponse> updateAdminKey(
            @Parameter(description = "The Hiero topic ID.", required = true, example = "0.0.55001")
            @PathVariable("topicId") String topicId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Current admin private key to authorise the rotation. A fresh key pair is generated server-side and returned.",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = UpdateTopicKeyRequest.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "adminPrivateKey": "302e020100300506032b657004220420aabbcc..."
                                    }""")))
            @RequestBody UpdateTopicKeyRequest request) throws Exception {
        PrivateKey newKey = PrivateKey.generateED25519();
        topicClient.updateAdminKey(
                TopicId.fromString(topicId),
                PrivateKey.fromString(request.adminPrivateKey()),
                newKey);
        return ResponseEntity.ok(TopicKeyRotationResponse.of(TopicId.fromString(topicId), newKey));
    }

    // Submit key rotation

    @PutMapping("/{topicId}/submit-key")
    @Operation(
            summary = "Rotate topic submit key",
            description = "Replaces the submit key of the specified private topic with a newly generated ED25519 key pair. "
                    + "The current admin private key is required to authorise the rotation. "
                    + "The new key pair is generated server-side and returned — save it securely.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Submit key rotated successfully. Returns the new key pair.",
                    content = @Content(
                            schema = @Schema(implementation = TopicKeyRotationResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "topicId": "0.0.55002",
                                      "newPrivateKey": "302e020100300506032b657004220420ddeeff...",
                                      "newPublicKey": "302a300506032b6570032100ddeeff..."
                                    }"""))),
            @ApiResponse(
                    responseCode = "500",
                    description = "Submit key could not be rotated.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<TopicKeyRotationResponse> updateSubmitKey(
            @Parameter(description = "The Hiero topic ID.", required = true, example = "0.0.55002")
            @PathVariable("topicId") String topicId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Current admin private key to authorise the rotation. A fresh submit key pair is generated server-side and returned.",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = UpdateTopicKeyRequest.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "adminPrivateKey": "302e020100300506032b657004220420aabbcc..."
                                    }""")))
            @RequestBody UpdateTopicKeyRequest request) throws Exception {
        PrivateKey newKey = PrivateKey.generateED25519();
        topicClient.updateSubmitKey(
                TopicId.fromString(topicId),
                PrivateKey.fromString(request.adminPrivateKey()),
                newKey);
        return ResponseEntity.ok(TopicKeyRotationResponse.of(TopicId.fromString(topicId), newKey));
    }

    // Atomic update — admin key, submit key, and memo in a single transaction

    @PutMapping("/{topicId}")
    @Operation(
            summary = "Update topic admin key, submit key, and memo",
            description = "Atomically rotates both the admin key and the submit key of the specified topic "
                    + "and updates the memo, all in a single network transaction. "
                    + "Both new key pairs are generated server-side and returned — save them securely.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Topic updated successfully. Returns both new key pairs.",
                    content = @Content(
                            schema = @Schema(implementation = TopicUpdatedResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "topicId": "0.0.55002",
                                      "newAdminPrivateKey": "302e020100300506032b657004220420aabbcc...",
                                      "newAdminPublicKey": "302a300506032b6570032100aabbcc...",
                                      "newSubmitPrivateKey": "302e020100300506032b657004220420ddeeff...",
                                      "newSubmitPublicKey": "302a300506032b6570032100ddeeff...",
                                      "memo": "Updated enterprise event bus"
                                    }"""))),
            @ApiResponse(
                    responseCode = "500",
                    description = "Topic could not be updated.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<TopicUpdatedResponse> updateTopic(
            @Parameter(description = "The Hiero topic ID.", required = true, example = "0.0.55002")
            @PathVariable("topicId") String topicId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Current admin key and new memo. Fresh admin and submit key pairs are generated server-side.",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = UpdateTopicRequest.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "currentAdminPrivateKey": "302e020100300506032b657004220420aabbcc...",
                                      "memo": "Updated enterprise event bus"
                                    }""")))
            @RequestBody UpdateTopicRequest request) throws Exception {
        PrivateKey currentAdminKey = PrivateKey.fromString(request.currentAdminPrivateKey());
        PrivateKey newAdminKey = PrivateKey.generateED25519();
        PrivateKey newSubmitKey = PrivateKey.generateED25519();
        topicClient.updateTopic(
                TopicId.fromString(topicId),
                currentAdminKey,
                newAdminKey,
                newSubmitKey,
                request.memo());
        return ResponseEntity.ok(TopicUpdatedResponse.of(TopicId.fromString(topicId), newAdminKey, newSubmitKey, request.memo()));
    }

    // Topic deletion

    @DeleteMapping("/{topicId}")
    @Operation(
            summary = "Delete a topic",
            description = "Permanently deletes the specified topic from the Hiero network. "
                    + "The topic's admin private key is required to authorise the deletion.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Topic deleted successfully.",
                    content = @Content(
                            schema = @Schema(implementation = SuccessResponse.class),
                            examples = @ExampleObject(value = """
                                    { "message": "Topic 0.0.55001 deleted successfully." }"""))),
            @ApiResponse(
                    responseCode = "500",
                    description = "Topic could not be deleted.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<SuccessResponse> deleteTopic(
            @Parameter(description = "The Hiero topic ID to delete.", required = true, example = "0.0.55001")
            @PathVariable("topicId") String topicId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Admin private key of the topic to authorise the deletion.",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = DeleteTopicRequest.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "adminPrivateKey": "302e020100300506032b657004220420aabbcc..."
                                    }""")))
            @RequestBody DeleteTopicRequest request) throws Exception {
        topicClient.deleteTopic(
                TopicId.fromString(topicId),
                PrivateKey.fromString(request.adminPrivateKey()));
        return ResponseEntity.ok(SuccessResponse.of("Topic " + topicId + " deleted successfully."));
    }

    // Message submission

    @PostMapping("/{topicId}/messages")
    @Operation(
            summary = "Submit a message to a topic",
            description = "Submits a text message to the specified topic. "
                    + "For public topics no submit key is needed. "
                    + "For private topics, provide the submit private key in the request body.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Message submitted successfully.",
                    content = @Content(
                            schema = @Schema(implementation = SuccessResponse.class),
                            examples = @ExampleObject(value = """
                                    { "message": "Message submitted to topic 0.0.55001 successfully." }"""))),
            @ApiResponse(
                    responseCode = "500",
                    description = "Message could not be submitted.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<SuccessResponse> submitMessage(
            @Parameter(description = "The Hiero topic ID.", required = true, example = "0.0.55001")
            @PathVariable("topicId") String topicId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Message content and optional submit key for private topics.",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = SubmitMessageRequest.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "message": "Hello, Hiero network!",
                                      "submitKey": null
                                    }""")))
            @RequestBody SubmitMessageRequest request) throws Exception {
        if (request.submitKey() == null || request.submitKey().isBlank()) {
            topicClient.submitMessage(TopicId.fromString(topicId), request.message());
        } else {
            topicClient.submitMessage(
                    TopicId.fromString(topicId),
                    PrivateKey.fromString(request.submitKey()),
                    request.message());
        }
        return ResponseEntity.ok(SuccessResponse.of("Message submitted to topic " + topicId + " successfully."));
    }

    @PostMapping("/{topicId}/messages/binary")
    @Operation(
            summary = "Submit a binary message to a topic",
            description = "Submits a binary message to the specified topic. "
                    + "The message must be provided as a Base64-encoded string. "
                    + "Use this for structured binary payloads such as serialised Protobuf. "
                    + "For private topics, provide the submit private key in the request body.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Binary message submitted successfully.",
                    content = @Content(
                            schema = @Schema(implementation = SuccessResponse.class),
                            examples = @ExampleObject(value = """
                                    { "message": "Binary message submitted to topic 0.0.55001 successfully." }"""))),
            @ApiResponse(
                    responseCode = "400",
                    description = "The provided messageBase64 is not valid Base64.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(
                    responseCode = "500",
                    description = "Binary message could not be submitted.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<SuccessResponse> submitBinaryMessage(
            @Parameter(description = "The Hiero topic ID.", required = true, example = "0.0.55001")
            @PathVariable("topicId") String topicId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Base64-encoded binary message and optional submit key for private topics.",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = SubmitBinaryMessageRequest.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "messageBase64": "SGVsbG8sIEhpZXJvIG5ldHdvcmsh",
                                      "submitKey": null
                                    }""")))
            @RequestBody SubmitBinaryMessageRequest request) throws Exception {
        byte[] messageBytes;
        try {
            messageBytes = Base64.getDecoder().decode(request.messageBase64());
        } catch (IllegalArgumentException e) {
            throw new org.springframework.web.server.ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "messageBase64 is not valid Base64: " + e.getMessage());
        }
        if (request.submitKey() == null || request.submitKey().isBlank()) {
            topicClient.submitMessage(TopicId.fromString(topicId), messageBytes);
        } else {
            topicClient.submitMessage(
                    TopicId.fromString(topicId),
                    PrivateKey.fromString(request.submitKey()),
                    messageBytes);
        }
        return ResponseEntity.ok(SuccessResponse.of("Binary message submitted to topic " + topicId + " successfully."));
    }

    // Message queries

    @GetMapping("/{topicId}/messages")
    @Operation(
            summary = "Get all messages for a topic",
            description = "Retrieves all messages submitted to the specified topic from the Hiero mirror node. "
                    + "Returns an empty list if no messages have been submitted yet.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Messages retrieved successfully.",
                    content = @Content(
                            schema = @Schema(implementation = TopicMessageResponse.class),
                            examples = @ExampleObject(value = """
                                    [
                                      {
                                        "topicId": "0.0.55001",
                                        "sequenceNumber": 1,
                                        "message": "Hello, Hiero network!",
                                        "consensusTimestamp": "2024-01-15T10:31:00Z",
                                        "payerAccountId": "0.0.12345",
                                        "runningHashBase64": "d2VsbGNvbWUgdG8gaGllcm8=",
                                        "runningHashVersion": 3,
                                        "chunked": false
                                      }
                                    ]"""))),
            @ApiResponse(
                    responseCode = "500",
                    description = "Could not retrieve messages.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<List<TopicMessageResponse>> getMessages(
            @Parameter(description = "The Hiero topic ID.", required = true, example = "0.0.55001")
            @PathVariable("topicId") String topicId) throws Exception {
        List<TopicMessageResponse> messages = topicRepository.getMessages(topicId)
                .getData()
                .stream()
                .map(TopicMessageResponse::from)
                .toList();
        return ResponseEntity.ok(messages);
    }

    @GetMapping("/{topicId}/messages/{sequenceNumber}")
    @Operation(
            summary = "Get a specific message by sequence number",
            description = "Retrieves a single message from the specified topic by its sequence number from the Hiero mirror node.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Message retrieved successfully.",
                    content = @Content(
                            schema = @Schema(implementation = TopicMessageResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "topicId": "0.0.55001",
                                      "sequenceNumber": 42,
                                      "message": "Hello, Hiero network!",
                                      "consensusTimestamp": "2024-01-15T10:31:00Z",
                                      "payerAccountId": "0.0.12345",
                                      "runningHashBase64": "d2VsbGNvbWUgdG8gaGllcm8=",
                                      "runningHashVersion": 3,
                                      "chunked": false
                                    }"""))),
            @ApiResponse(
                    responseCode = "404",
                    description = "Message not found for the given topic and sequence number.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(
                    responseCode = "500",
                    description = "Could not retrieve message.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<TopicMessageResponse> getMessageBySequenceNumber(
            @Parameter(description = "The Hiero topic ID.", required = true, example = "0.0.55001")
            @PathVariable("topicId") String topicId,
            @Parameter(description = "The sequence number of the message.", required = true, example = "42")
            @PathVariable("sequenceNumber") long sequenceNumber) throws Exception {
        return topicRepository.getMessageBySequenceNumber(topicId, sequenceNumber)
                .map(TopicMessageResponse::from)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Message not found for topic " + topicId + " at sequence number " + sequenceNumber));
    }
}
