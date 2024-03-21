package response;

import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.ollama.OllamaStreamingChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.service.AiServices;
import infra.tools.DrawingTools;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.util.Optional;

import static org.apache.commons.io.IOUtils.write;

public class ChatModelFactory {

    private ChatModelFactory() {
    }

    public static StreamingChatLanguageModel generateStreaming(String url, String model) {
        return switch (model) {
            case "ollama-mistral" -> {
                StreamingChatLanguageModel ollama = OllamaStreamingChatModel.builder()
                        .baseUrl(url)
                        .modelName(model.replace("ollama-", ""))
                        .temperature(0.9)
                        .build();

                var chatModel = AiServices.builder(StreamingChatLanguageModel.class)
                        .streamingChatLanguageModel(ollama);


                yield addMemory(chatModel, true).build();
            }
            case "openai" -> {
                String openapiKey = Optional
                        .ofNullable(ChatModelFactory.class.getClassLoader().getResourceAsStream("secret/openapi"))
                        .map(inputStream -> {
                            try {
                                return inputStream.readAllBytes();
                            } catch (IOException e) {
                                throw new IllegalArgumentException("Create a file called openapi in src/resources/secret with your openapi. And don't share it !");
                            }
                        })
                        .map(String::new)
                        .orElseThrow(() -> new IllegalArgumentException("Create a file called openapi in src/resources/secret with your openapi. And don't share it !"));
                var chatModel = OpenAiStreamingChatModel.builder()
                        .apiKey(openapiKey) // Please use your own OpenAI API key
                        .maxTokens(50)
                        .build();

                var openAI = AiServices.builder(StreamingChatLanguageModel.class)
                        .streamingChatLanguageModel(chatModel);

                yield addMemory(openAI, true).build();
            }
            default -> throw new IllegalArgumentException("Model %s not supported".formatted(model));
        };
    }


    public static GenericChatModel generate(String url, String model, boolean memory) {

        return switch (model) {
            case "ollama-mistral" -> {
                ChatLanguageModel ollama = OllamaChatModel.builder()
                        .baseUrl(url)
                        .modelName(model.replace("ollama-", ""))
                        .temperature(0.9)
                        .build();

                var chatModel = AiServices.builder(ChatLanguageModel.class)
                        .chatLanguageModel(ollama);
                yield standard(addMemory(chatModel, memory).build());
            }
            case "openai" -> {
                String openapiKey = Optional
                        .ofNullable(ChatModelFactory.class.getClassLoader().getResourceAsStream("secret/openapi"))
                        .map(inputStream -> {
                            try {
                                return inputStream.readAllBytes();
                            } catch (IOException e) {
                                throw new IllegalArgumentException("Create a file called openapi in src/resources/secret with your openapi. And don't share it !");
                            }
                        })
                        .map(String::new)
                        .orElseThrow(() -> new IllegalArgumentException("Create a file called openapi in src/resources/secret with your openapi. And don't share it !"));


                var chatModel = OpenAiChatModel.builder()
                        .apiKey(openapiKey) // Please use your own OpenAI API key
                        .maxTokens(50)
                        .build();

                var openAI = AiServices.builder(Assistant.class)
                        .chatLanguageModel(chatModel);

                yield standard(addMemory(openAI, memory).build());

            }
            default -> throw new IllegalArgumentException("Model %s not supported".formatted(model));

        };
    }

    interface Assistant {
        String chat(String userMessage);
    }

    private static <T> AiServices<T> addMemory(AiServices<T> aiService, boolean memory) {
        if (memory) {
            aiService.chatMemory(MessageWindowChatMemory.withMaxMessages(10))
                    //Memory is mandatory for tools
                    .tools(new DrawingTools());
        }
        return aiService;
    }

    private static GenericChatModel standard(ChatLanguageModel chatModel) {
        return (userMessage, responseOutput) -> write(chatModel.generate(userMessage).getBytes(), responseOutput.get());
    }

    private static GenericChatModel standard(Assistant chatModel) {
        return (userMessage, responseOutput) -> write(chatModel.chat(userMessage).getBytes(), responseOutput.get());
    }


}
