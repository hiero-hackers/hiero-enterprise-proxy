package org.hiero.proxy.server.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.stream.Collectors;
import org.hiero.base.data.TransactionInfo;

@Schema(description = "Detailed information about a Hiero transaction retrieved from the mirror node.")
public record TransactionInfoResponse(
        @Schema(description = "The unique transaction ID.", example = "0.0.12345@1700000000.000000000")
        String transactionId,

        @Schema(description = "The transaction type.", example = "CRYPTO_TRANSFER")
        String transactionType,

        @Schema(description = "The transaction result.", example = "SUCCESS")
        String result,

        @Schema(description = "Whether this was a scheduled transaction.", example = "false")
        boolean scheduled,

        @Schema(description = "Transaction fee charged in tinybar.", example = "855875")
        long chargedTxFee,

        @Schema(description = "Maximum fee the payer was willing to pay in tinybar.", example = "1000000")
        String maxFee,

        @Schema(description = "Unix epoch second of consensus timestamp.", example = "1700000000")
        long consensusTimestampEpochSecond,

        @Schema(description = "Unix epoch second of valid-start timestamp.", example = "1699999999")
        long validStartTimestampEpochSecond,

        @Schema(description = "Duration in seconds the transaction is valid.", example = "120")
        String validDurationSeconds,

        @Schema(description = "The entity created or modified by this transaction (account, token, topic, etc.).",
                example = "0.0.98765", nullable = true)
        String entityId,

        @Schema(description = "The node that processed the transaction.", example = "0.0.3", nullable = true)
        String node,

        @Schema(description = "Nonce value for the transaction.", example = "0")
        int nonce,

        @Schema(description = "Consensus timestamp of the parent transaction, if this is a child.",
                nullable = true, example = "1699999998")
        Long parentConsensusTimestampEpochSecond,

        @Schema(description = "HBAR transfers within this transaction.")
        List<HbarTransferResponse> transfers,

        @Schema(description = "Fungible token transfers within this transaction.")
        List<TokenTransferResponse> tokenTransfers,

        @Schema(description = "NFT transfers within this transaction.")
        List<NftTransferResponse> nftTransfers,

        @Schema(description = "Staking reward transfers within this transaction.")
        List<StakingRewardTransferResponse> stakingRewardTransfers
) {
    /** Maps a domain {@link TransactionInfo} to the API representation. */
    public static TransactionInfoResponse from(TransactionInfo tx) {
        return new TransactionInfoResponse(
                tx.transactionId(),
                tx.name().name(),
                tx.result(),
                tx.scheduled(),
                tx.chargedTxFee(),
                tx.maxFee(),
                tx.consensusTimestamp().getEpochSecond(),
                tx.validStartTimestamp().getEpochSecond(),
                tx.validDurationSeconds(),
                tx.entityId(),
                tx.node(),
                tx.nonce(),
                tx.parentConsensusTimestamp() != null
                        ? tx.parentConsensusTimestamp().getEpochSecond()
                        : null,
                tx.transfers().stream()
                        .map(HbarTransferResponse::from)
                        .collect(Collectors.toList()),
                tx.tokenTransfers().stream()
                        .map(TokenTransferResponse::from)
                        .collect(Collectors.toList()),
                tx.nftTransfers().stream()
                        .map(NftTransferResponse::from)
                        .collect(Collectors.toList()),
                tx.stakingRewardTransfers().stream()
                        .map(StakingRewardTransferResponse::from)
                        .collect(Collectors.toList())
        );
    }
}
