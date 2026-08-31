package com.infy.controller;

import java.util.List;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Aviation Travel Experience Assistant — demonstrates two levels of ChatModel API.
 *
 * 1) GET /api/chat/ask
 *    Raw call: chatModel.call(String). No system prompt, no metadata.
 *    Shows the model's unguided response to a passenger travel query.
 *
 * 2) GET /api/chat/ask-with-metadata
 *    Structured call: chatModel.call(Prompt) with a senior aviation customer
 *    experience consultant system prompt. Returns tailored travel guidance
 *    plus token-usage cost breakdown.
 *    Shows how a system prompt improves output quality, and how ChatResponse
 *    exposes metadata useful for cost monitoring and rate-limit awareness.
 */
@RestController
@RequestMapping("/api/chat")
@CrossOrigin
public class AIController {

	private static final String DEFAULT_TRAVEL_QUERY =
			"What digital technologies and self-service tools can airlines offer " +
			"to improve the end-to-end passenger journey from booking to baggage claim?";

	private final ChatModel chatModel;

	public AIController(ChatModel chatModel) {
		this.chatModel = chatModel;
	}

	/**
	 * Raw travel query: no system prompt, plain string call.
	 * The model answers without any role or structure constraint.
	 * Use this to contrast with /ask-with-metadata.
	 *
	 * Example: /api/chat/ask?userPrompt=How can I use the airline app to manage my trip?
	 */
	@GetMapping(value = "/ask", produces = MediaType.TEXT_PLAIN_VALUE)
	public String askTravelQuery(
			@RequestParam(defaultValue = DEFAULT_TRAVEL_QUERY) String userPrompt) {
		return chatModel.call(userPrompt);
	}

	/**
	 * Guided travel assistance: system prompt shapes the model into a senior
	 * aviation customer experience consultant. Returns structured travel guidance
	 * plus token-usage cost breakdown.
	 *
	 * Example: /api/chat/ask-with-metadata?userPrompt=How can I reduce airport wait times?
	 */
	@GetMapping(value = "/ask-with-metadata", produces = MediaType.TEXT_PLAIN_VALUE)
	public String askTravelQueryWithCost(
			@RequestParam(defaultValue = DEFAULT_TRAVEL_QUERY) String userPrompt) {

		Prompt prompt = new Prompt(List.of(
				new SystemMessage("""
						You are a senior aviation customer experience consultant specializing in digital transformation for airlines.
						Your goal is to help passengers and airline staff improve the travel journey using modern digital technologies.
						Topics you cover include: mobile check-in, biometric boarding, real-time flight updates, AI-powered chatbots,
						personalized loyalty programs, smart baggage tracking, lounge access, and in-flight connectivity.
						Format your response as a numbered list of actionable recommendations.
						Respond in plain text only. Do not use markdown symbols.
						"""),
				new UserMessage(userPrompt)));

		ChatResponse response = chatModel.call(prompt);

		String content = response.getResult().getOutput().getText();
		Integer promptTokens      = response.getMetadata().getUsage().getPromptTokens();
		Integer completionTokens  = response.getMetadata().getUsage().getCompletionTokens();
		Integer totalTokens       = response.getMetadata().getUsage().getTotalTokens();
		Long    remainingTokens   = response.getMetadata().getRateLimit().getTokensRemaining();
		String  model             = response.getMetadata().getModel();

		return content
				+ "\n\n--- query cost ---"
				+ "\nModel:             " + model
				+ "\nPrompt tokens:     " + promptTokens
				+ "\nCompletion tokens: " + completionTokens
				+ "\nTotal tokens:      " + totalTokens
				+ "\nRemaining tokens:  " + remainingTokens;
	}
}
