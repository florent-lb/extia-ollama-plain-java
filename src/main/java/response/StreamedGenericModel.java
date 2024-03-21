package response;

import dev.langchain4j.model.chat.StreamingChatLanguageModel;

public interface StreamedGenericModel extends GenericChatModel, StreamingChatLanguageModel {
}
