package org.hiero.proxy.server.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.hiero.base.mirrornode.NetworkRepository;
import org.hiero.proxy.server.dto.response.ExchangeRatesResponse;
import org.hiero.proxy.server.dto.response.NetworkFeeResponse;
import org.hiero.proxy.server.dto.response.NetworkStakeResponse;
import org.hiero.proxy.server.dto.response.NetworkSuppliesResponse;
import org.hiero.proxy.server.exception.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/network")
@Tag(name = "Network", description = "Read-only operations for querying Hiero network information — "
        + "exchange rates, transaction fees, staking statistics, and HBAR supply figures. "
        + "All data is sourced from the Hiero mirror node.")
public class NetworkController {

    private final NetworkRepository networkRepository;

    public NetworkController(NetworkRepository networkRepository) {
        this.networkRepository = Objects.requireNonNull(networkRepository, "networkRepository must not be null");
    }

    // ─── Exchange Rates ───────────────────────────────────────────────────────

    @GetMapping("/exchange-rates")
    @Operation(
            summary = "Get HBAR/USD exchange rates",
            description = "Fetches the current and next HBAR/USD exchange rates from the Hiero mirror node. "
                    + "The current rate is active now; the next rate takes effect when the current one expires. "
                    + "Rates are expressed as a ratio: `centEquivalent / hbarEquivalent` gives the USD cent value per HBAR.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Exchange rates retrieved successfully.",
                    content = @Content(
                            schema = @Schema(implementation = ExchangeRatesResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "currentRate": {
                                        "centEquivalent": 12,
                                        "hbarEquivalent": 1,
                                        "expirationTimeEpochSecond": 1700000000
                                      },
                                      "nextRate": {
                                        "centEquivalent": 13,
                                        "hbarEquivalent": 1,
                                        "expirationTimeEpochSecond": 1700003600
                                      }
                                    }"""))),
            @ApiResponse(
                    responseCode = "404",
                    description = "Exchange rate data not available on the mirror node.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(
                    responseCode = "500",
                    description = "Could not retrieve exchange rates.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ExchangeRatesResponse> getExchangeRates() throws Exception {
        return networkRepository.exchangeRates()
                .map(ExchangeRatesResponse::from)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Exchange rate data is not available."));
    }

    // ─── Network Fees ─────────────────────────────────────────────────────────

    @GetMapping("/fees")
    @Operation(
            summary = "Get EVM gas fees",
            description = "Fetches the current EVM gas fees for smart contract and Ethereum transaction types "
                    + "from the Hiero mirror node (sourced from `/api/v1/network/fees`). "
                    + "This covers EVM-based operations such as ContractCall, ContractCreate, and EthereumTransaction. "
                    + "Note: HBAR transfer and HTS token fees (CRYPTOTRANSFER, TOKENMINT, etc.) use a separate "
                    + "USD-denominated fee schedule and are not included here. "
                    + "Returns an empty list if no fee schedule is currently published.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "EVM gas fees retrieved successfully.",
                    content = @Content(
                            schema = @Schema(implementation = NetworkFeeResponse.class, type = "array"),
                            examples = @ExampleObject(value = """
                                    [
                                      { "gas": 171, "transactionType": "ContractCall" },
                                      { "gas": 371, "transactionType": "ContractCreate" },
                                      { "gas": 113, "transactionType": "EthereumTransaction" }
                                    ]"""))),
            @ApiResponse(
                    responseCode = "500",
                    description = "Could not retrieve network fees.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<List<NetworkFeeResponse>> getFees() throws Exception {
        return ResponseEntity.ok(
                networkRepository.fees()
                        .stream()
                        .map(NetworkFeeResponse::from)
                        .collect(Collectors.toList())
        );
    }

    // ─── Network Stake ────────────────────────────────────────────────────────

    @GetMapping("/stake")
    @Operation(
            summary = "Get network staking information",
            description = "Fetches the current staking configuration and statistics for the Hiero network "
                    + "from the mirror node. Includes total staked HBAR, reward rates, staking period duration, "
                    + "and reward threshold values.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Network staking information retrieved successfully.",
                    content = @Content(
                            schema = @Schema(implementation = NetworkStakeResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "maxStakeReward": 17500000000000000,
                                      "maxStakeRewardPerHbar": 6849,
                                      "maxTotalReward": 17500000000000000,
                                      "nodeRewardFeeFraction": 0.0,
                                      "reservedStakingRewards": 50000000000000000,
                                      "rewardBalanceThreshold": 25000000000000000,
                                      "stakeTotal": 5000000000000000,
                                      "stakingPeriodDuration": 1440,
                                      "stakingPeriodsStored": 365,
                                      "stakingRewardFeeFraction": 0.1,
                                      "stakingRewardRate": 100,
                                      "stakingStartThreshold": 25000000000000000,
                                      "unreservedStakingRewardBalance": 12000000000000000
                                    }"""))),
            @ApiResponse(
                    responseCode = "404",
                    description = "Staking data not available on the mirror node.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(
                    responseCode = "500",
                    description = "Could not retrieve staking information.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<NetworkStakeResponse> getStake() throws Exception {
        return networkRepository.stake()
                .map(NetworkStakeResponse::from)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Network staking data is not available."));
    }

    // ─── Network Supplies ─────────────────────────────────────────────────────

    @GetMapping("/supplies")
    @Operation(
            summary = "Get HBAR supply information",
            description = "Fetches the current HBAR supply figures from the Hiero mirror node. "
                    + "`releasedSupply` is the amount of HBAR currently in circulation. "
                    + "`totalSupply` includes all HBAR including unreleased treasury holdings. "
                    + "Both values are expressed in tinybar (1 HBAR = 100,000,000 tinybar).")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "HBAR supply information retrieved successfully.",
                    content = @Content(
                            schema = @Schema(implementation = NetworkSuppliesResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "releasedSupply": "3987242498080780000",
                                      "totalSupply": "5000000000000000000"
                                    }"""))),
            @ApiResponse(
                    responseCode = "404",
                    description = "Supply data not available on the mirror node.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(
                    responseCode = "500",
                    description = "Could not retrieve supply information.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<NetworkSuppliesResponse> getSupplies() throws Exception {
        return networkRepository.supplies()
                .map(NetworkSuppliesResponse::from)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Network supply data is not available."));
    }
}
