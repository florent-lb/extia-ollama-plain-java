package response;

import dev.langchain4j.service.UserMessage;
import infra.ResponseOutput;

import java.io.IOException;

@FunctionalInterface
public interface GenericChatModel {

    void generate(@UserMessage String userMessage, ResponseOutput responseOutput) throws IOException;
}
