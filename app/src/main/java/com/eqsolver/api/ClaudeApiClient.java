package com.eqsolver.api;

import android.os.Handler;
import android.os.Looper;

import com.eqsolver.BuildConfig;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Client for interacting with the Claude API to solve mathematical equations.
 */
public class ClaudeApiClient {
    private static final String API_URL = "https://api.anthropic.com/v1/messages";
    private static final String ANTHROPIC_VERSION = "2023-06-01";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private final OkHttpClient client;
    private final Gson gson;
    private final ExecutorService executorService;
    private final Handler mainHandler;

    public ClaudeApiClient() {
        this.client = new OkHttpClient();
        this.gson = new Gson();
        this.executorService = Executors.newSingleThreadExecutor();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    /**
     * Solves an equation using Claude API with step-by-step explanation.
     *
     * @param equation The mathematical equation to solve
     * @param callback Callback for handling response or errors
     */
    public void solveEquation(String equation, SolutionCallback callback) {
        executorService.execute(() -> {
            try {
                String jsonBody = buildRequestBody(equation);
                Request request = new Request.Builder()
                        .url(API_URL)
                        .addHeader("x-api-key", BuildConfig.CLAUDE_API_KEY)
                        .addHeader("anthropic-version", ANTHROPIC_VERSION)
                        .addHeader("content-type", "application/json")
                        .post(RequestBody.create(jsonBody, JSON))
                        .build();

                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        postToMainThread(() -> callback.onError("Network error: " + e.getMessage()));
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (!response.isSuccessful()) {
                            String errorMsg = "API error: " + response.code();
                            if (response.body() != null) {
                                errorMsg += " - " + response.body().string();
                            }
                            String finalErrorMsg = errorMsg;
                            postToMainThread(() -> callback.onError(finalErrorMsg));
                            return;
                        }

                        try {
                            String responseBody = response.body().string();
                            String solution = parseSolution(responseBody);
                            postToMainThread(() -> callback.onSuccess(solution));
                        } catch (Exception e) {
                            postToMainThread(() -> callback.onError("Failed to parse response: " + e.getMessage()));
                        }
                    }
                });
            } catch (Exception e) {
                postToMainThread(() -> callback.onError("Request failed: " + e.getMessage()));
            }
        });
    }

    /**
     * Builds the JSON request body for the Claude API.
     */
    private String buildRequestBody(String equation) {
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("model", "claude-3-5-sonnet-20240620");
        requestBody.addProperty("max_tokens", 2048);

        JsonArray messages = new JsonArray();
        JsonObject message = new JsonObject();
        message.addProperty("role", "user");

        String prompt = "Please solve the following mathematical equation and show your work step by step. " +
                "Be clear and detailed in your explanation so a student can understand how to solve similar problems.\n\n" +
                "Equation: " + equation + "\n\n" +
                "Please provide:\n" +
                "1. The final answer\n" +
                "2. Step-by-step solution with explanations for each step";

        message.addProperty("content", prompt);
        messages.add(message);
        requestBody.add("messages", messages);

        return gson.toJson(requestBody);
    }

    /**
     * Parses the Claude API response to extract the solution text.
     */
    private String parseSolution(String responseBody) {
        JsonObject jsonResponse = gson.fromJson(responseBody, JsonObject.class);
        JsonArray content = jsonResponse.getAsJsonArray("content");

        if (content != null && content.size() > 0) {
            JsonObject firstContent = content.get(0).getAsJsonObject();
            return firstContent.get("text").getAsString();
        }

        return "Unable to parse solution from response.";
    }

    /**
     * Posts a runnable to the main thread.
     */
    private void postToMainThread(Runnable runnable) {
        mainHandler.post(runnable);
    }

    /**
     * Shuts down the executor service. Call this when done with the client.
     */
    public void shutdown() {
        executorService.shutdown();
    }

    /**
     * Callback interface for equation solving results.
     */
    public interface SolutionCallback {
        void onSuccess(String solution);
        void onError(String error);
    }
}
