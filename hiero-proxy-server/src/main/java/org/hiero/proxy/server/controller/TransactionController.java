package org.hiero.proxy.server.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.hiero.base.data.BalanceModification;
import org.hiero.base.data.Result;
import org.hiero.base.mirrornode.TransactionRepository;
import org.hiero.base.protocol.data.TransactionType;
import org.hiero.proxy.server.dto.response.TransactionInfoResponse;
import org.hiero.proxy.server.exception.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/transactions")
@Tag(
        name = "Transactions",
        description = "Read-only operations for querying Hiero transactions from the mirror node. "
                + "Transactions can be looked up by ID or filtered by account with optional "
                + "type, result, and balance-modification filters.")
public class TransactionController {

    private final TransactionRepository transactionRepository;

    public TransactionController(TransactionRepository transactionRepository) {
        this.transactionRepository = Objects.requireNonNull(
                transactionRepository, "transactionRepository must not be null");
    }

    // Get by ID

    @GetMapping("/{transactionId}")
    @Operation(
            summary = "Get transaction by ID",
            description = "Fetches a specific transaction by its Hedera transaction ID from the mirror node. "
                    + "The ID format is `{accountId}@{seconds}.{nanos}`, "
                    + "e.g. `0.0.12345@1700000000.000000000`.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Transaction retrieved successfully.",
                    content = @Content(
                            schema = @Schema(implementation = TransactionInfoResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "transactionId": "0.0.12345@1700000000.000000000",
                                      "transactionType": "CRYPTO_TRANSFER",
                                      "result": "SUCCESS",
                                      "scheduled": false,
                                      "chargedTxFee": 855875,
                                      "maxFee": "1000000",
                                      "consensusTimestampEpochSecond": 1700000000,
                                      "validStartTimestampEpochSecond": 1699999999,
                                      "validDurationSeconds": "120",
                                      "entityId": null,
                                      "node": "0.0.3",
                                      "nonce": 0,
                                      "parentConsensusTimestampEpochSecond": null,
                                      "transfers": [
                                        { "accountId": "0.0.12345", "amount": -1000000000, "isApproval": false },
                                        { "accountId": "0.0.98765", "amount": 1000000000, "isApproval": false }
                                      ],
                                      "tokenTransfers": [],
                                      "nftTransfers": [],
                                      "stakingRewardTransfers": []
                                    }"""))),
            @ApiResponse(
                    responseCode = "404",
                    description = "Transaction not found.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(
                    responseCode = "500",
                    description = "Could not retrieve transaction.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<TransactionInfoResponse> getById(
            @Parameter(
                    description = "The Hedera transaction ID in format `{accountId}@{seconds}.{nanos}`.",
                    required = true,
                    example = "0.0.12345@1700000000.000000000")
            @PathVariable("transactionId") String transactionId) throws Exception {

        return transactionRepository.findById(transactionId)
                .map(TransactionInfoResponse::from)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Transaction not found: " + transactionId));
    }

    // List by Account

    @GetMapping("/account/{accountId}")
    @Operation(
            summary = "List transactions by account",
            description = "Fetches transactions associated with the specified account from the mirror node. "
                    + "Use the optional `type`, `result`, and `modification` query parameters to narrow results.\n\n"
                    + "**`type`** — one of the `TransactionType` enum values, e.g. `CRYPTO_TRANSFER`, "
                    + "`TOKEN_MINT`, `TOPIC_MESSAGE_SUBMIT`. See the full list in the schema.\n\n"
                    + "**`result`** — `SUCCESS` or `FAIL`.\n\n"
                    + "**`modification`** — `CREDIT` (transactions that added balance) "
                    + "or `DEBIT` (transactions that reduced balance).\n\n"
                    + "Only one filter can be applied at a time. If more than one is provided, "
                    + "`type` takes precedence, then `result`, then `modification`.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Transactions retrieved successfully.",
                    content = @Content(
                            schema = @Schema(implementation = TransactionInfoResponse.class, type = "array"))),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid `type`, `result`, or `modification` value.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(
                    responseCode = "500",
                    description = "Could not retrieve transactions.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<List<TransactionInfoResponse>> listByAccount(
            @Parameter(description = "The Hedera account ID.", required = true, example = "0.0.12345")
            @PathVariable("accountId") String accountId,

            @Parameter(
                    description = "Filter by transaction type, e.g. `CRYPTO_TRANSFER`, `TOKEN_MINT`.",
                    example = "CRYPTO_TRANSFER")
            @RequestParam(value = "type", required = false) String type,

            @Parameter(
                    description = "Filter by transaction result: `SUCCESS` or `FAIL`.",
                    example = "SUCCESS")
            @RequestParam(value = "result", required = false) String result,

            @Parameter(
                    description = "Filter by balance modification direction: `CREDIT` or `DEBIT`.",
                    example = "CREDIT")
            @RequestParam(value = "modification", required = false) String modification
    ) throws Exception {

        // type filter takes precedence
        if (type != null) {
            TransactionType txType;
            try {
                txType = TransactionType.valueOf(type.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Unknown transaction type: '" + type + "'. "
                                + "Valid values are the TransactionType enum names, e.g. CRYPTO_TRANSFER.");
            }
            return ResponseEntity.ok(
                    transactionRepository.findByAccountAndType(accountId, txType)
                            .getData()
                            .stream()
                            .map(TransactionInfoResponse::from)
                            .collect(Collectors.toList())
            );
        }

        // result filter
        if (result != null) {
            Result txResult;
            try {
                txResult = Result.valueOf(result.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Unknown result: '" + result + "'. Valid values: SUCCESS, FAIL.");
            }
            return ResponseEntity.ok(
                    transactionRepository.findByAccountAndResult(accountId, txResult)
                            .getData()
                            .stream()
                            .map(TransactionInfoResponse::from)
                            .collect(Collectors.toList())
            );
        }

        // modification filter
        if (modification != null) {
            BalanceModification mod;
            try {
                mod = BalanceModification.valueOf(modification.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Unknown modification: '" + modification + "'. Valid values: CREDIT, DEBIT.");
            }
            return ResponseEntity.ok(
                    transactionRepository.findByAccountAndModification(accountId, mod)
                            .getData()
                            .stream()
                            .map(TransactionInfoResponse::from)
                            .collect(Collectors.toList())
            );
        }

        // no filter — return all
        return ResponseEntity.ok(
                transactionRepository.findByAccount(accountId)
                        .getData()
                        .stream()
                        .map(TransactionInfoResponse::from)
                        .collect(Collectors.toList())
        );
    }
}
