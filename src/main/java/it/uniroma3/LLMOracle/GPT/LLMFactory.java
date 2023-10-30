package it.uniroma3.LLMOracle.GPT;

import it.uniroma3.LLMOracle.Application;

public class LLMFactory {

    private static final String STANDARD_ASSISTANT_CONTENT = "You will be given 2 snippets of texts. You will have to answer whether the 2 texts are talking about the same entity, object or subject. Answer only with yes or no.";

    private static final String LLMImplementationClassName = Application.appProperties.getLLMImplementationClassName();


    public static LLM createLLMAllDefault() throws GPTException {
        try{
            return (LLM) Class.forName(LLMImplementationClassName).getConstructor(String.class).newInstance(STANDARD_ASSISTANT_CONTENT);
        }catch(Exception e){
            throw new GPTException(e.getMessage(), e.getCause());
        }
    }

    public static LLM createLLMWithAssistantContent(String assistantContent) throws GPTException {
        try{
            return (LLM) Class.forName(LLMImplementationClassName).getConstructor(String.class).newInstance(assistantContent);
        }catch(Exception e){
            throw new GPTException(e.getMessage(), e.getCause());
        }
    }

    public static LLM createLLMWithAssistantContentAndInitializationChat(String assistantContent, String initializationChat) throws GPTException {
        try{
            return (LLM) Class.forName(LLMImplementationClassName).getConstructor(String.class, String.class).newInstance(assistantContent, initializationChat);
        }catch(Exception e){
            throw new GPTException(e.getMessage(), e.getCause());
        }
    }

    /**
     * Static method that creates an LLM.
     * @param params A String array composed of the parameters needed to create the LLM. The order of the parameters is the following: assistantContent, initialization chat, api key. The parameters are all optional.
     * @return An LLM object.
     * @throws GPTException If the parameters are not correct.
     */
    /*public static LLM createLLM(String... params) throws GPTException {
        LLM llm = null;
        try {
            if (params.length == 0) {
                llm = (LLM) Class.forName(LLMImplementationClassName).getConstructor(String.class).newInstance(STANDARD_ASSISTANT_CONTENT);
            } else if (params.length == 1) {
                llm = (LLM) Class.forName(LLMImplementationClassName).getConstructor(String.class).newInstance(params[0]);
            } else if (params.length == 2) {
                llm = (LLM) Class.forName(LLMImplementationClassName).getConstructor(String.class, String.class).newInstance(params[0], params[1]);
            } else if (params.length == 3){
                llm = (LLM) Class.forName(LLMImplementationClassName).getConstructor(String.class, String.class, String.class).newInstance(params[0], params[1], params[2]);
            } else {
                throw new GPTException("Too many parameters provided to LLMFactory");
            }
            return llm;
        }catch(Exception e){
            throw new GPTException(e.getMessage(), e.getCause());
        }
    }*/
}
