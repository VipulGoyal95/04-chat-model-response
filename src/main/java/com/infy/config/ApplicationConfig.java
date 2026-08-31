package com.infy.config;

import org.springframework.ai.bedrock.converse.BedrockChatOptions;
import org.springframework.ai.bedrock.converse.BedrockProxyChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;

/**
 * MANUAL ChatModel configuration (instead of Spring AI auto-configuration).
 *
 * Spring AI's Bedrock Converse starter can auto-build a ChatModel bean purely
 * from application.properties. Here we deliberately build it ourselves so the
 * demo has full control over every piece of the wiring.
 *
 * Why do it manually:
 * - Explicit credentials: we construct AwsBasicCredentials and
 *   StaticCredentialsProvider directly from properties, rather than relying
 *   on the default AWS credential-chain discovery.
 * - Explicit region: we choose the AWS region ourselves in one place.
 * - Explicit defaults: model and maxTokens are set via BedrockChatOptions in
 *   code, rather than scattered across auto-config conventions.
 * - Extensible: easy to define multiple ChatModel beans, swap credentials
 *   strategies, or configure timeouts without fighting the auto-configuration.
 * - Returns the framework-neutral ChatModel type, so controller/service code
 *   stays provider-agnostic.
 *
 * The three steps below mirror what auto-configuration does behind the scenes:
 * 1) credentials setup (AwsBasicCredentials + StaticCredentialsProvider),
 * 2) model defaults (BedrockChatOptions),
 * 3) final model assembly (BedrockProxyChatModel).
 */
@Configuration
public class ApplicationConfig {

	@Value("${spring.ai.bedrock.aws.access-key}")
	private String accessKey;

	@Value("${spring.ai.bedrock.aws.secret-key}")
	private String secretKey;

	@Value("${spring.ai.bedrock.aws.region}")
	private String region;

	@Value("${spring.ai.bedrock.converse.chat.options.model}")
	private String model;

	@Value("${spring.ai.bedrock.converse.chat.options.max-tokens}")
	private Integer maxTokens;

	@Bean
	public BedrockProxyChatModel chatModel() {
		// 1) Build AWS credentials from the configured access/secret key pair.
		StaticCredentialsProvider credentialsProvider = StaticCredentialsProvider.create(
				AwsBasicCredentials.create(accessKey, secretKey));

		// 2) Define application-wide defaults for chat generation behavior.
		BedrockChatOptions defaultOptions = BedrockChatOptions.builder()
				.model(model)
				.maxTokens(maxTokens)
				.build();

		// 3) Assemble the ChatModel with explicit credentials, region + defaults.
		return BedrockProxyChatModel.builder()
				.credentialsProvider(credentialsProvider)
				.region(Region.of(region))
				.options(defaultOptions)
				.build();
	}
}
