import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.StreamingResponseHandler;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.output.Response;
import response.ChatModelFactory;
import response.GenericChatModel;

import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;
import java.util.Scanner;
import java.util.function.BiFunction;

public class Main {

    public static final String DEFAULT_OLLAMA_PORT = "11434";
    public static final String DEFAULT_MODEL = "ollama-mistral";
    private static final BiFunction<String[], Param, String> extractArgsParamValue = (args, param) -> Arrays.stream(args)
            .filter(Objects::nonNull)
            .filter(cli -> cli.contains("--%s=".formatted(param.paramName)))
            .map(cli -> cli.split("=")[1])
            .findFirst().orElse(param.defaultValue);


    /*
    --model : Model à utiliser par défaut ollama-mistral
    --port for ollama or the model : par défaut 11434
    --memory utiliser de la mémoire pour l'IA
    --streaming passer en mode stream (non fonctionnel)
     */
    public static void main(String... args) throws IOException {

        var model = extractArgsParamValue.apply(args, Param.MODEL);

        var port = extractArgsParamValue.apply(args, Param.PORT);
        var memory = Boolean.parseBoolean(extractArgsParamValue.apply(args, Param.MEMORY));
        var streaming = Boolean.parseBoolean(extractArgsParamValue.apply(args, Param.STREAMING));
        StreamingChatLanguageModel chatStreamModel = null;
        GenericChatModel chatModel = null;

        if(streaming)
        {
            chatStreamModel = ChatModelFactory.generateStreaming( baseUrl(port), model);
        }else {
            chatModel = ChatModelFactory.generate( baseUrl(port), model,memory);
        }


        System.out.println("Ask you question : ");
        try (Scanner sc = new Scanner(System.in)) {
            String question = "BYE";
            do {
                if (sc.hasNextLine()) {
                    question = sc.nextLine();
                    if (!question.isBlank() && !streaming)
                    {
                        chatModel.generate(question, () -> System.out);
                    }
                    else if(!question.isBlank()){
                        chatStreamModel.generate(question, new StreamingResponseHandler<AiMessage>() {
                            @Override
                            public void onNext(String token) {
                                System.out.println("onNext: " + token);
                            }

                            @Override
                            public void onComplete(Response<AiMessage> response) {
                                System.out.println("onComplete: " + response);
                            }

                            @Override
                            public void onError(Throwable error) {
                                error.printStackTrace();
                            }
                        });
                    }
                }
            } while (!"BYE".equalsIgnoreCase(question));
        }

    }

    static String baseUrl(String port) {
        return String.format("http://localhost:%s", port);
    }

    private enum Param {
        MODEL("model", DEFAULT_MODEL),
        PORT("port", DEFAULT_OLLAMA_PORT),
        MEMORY("memory", "false"),
        STREAMING("streaming", "false");

        private final String paramName;
        private final String defaultValue;

        Param(String paramName, String defaultValue) {
            this.paramName = paramName;
            this.defaultValue = defaultValue;
        }
    }

}
