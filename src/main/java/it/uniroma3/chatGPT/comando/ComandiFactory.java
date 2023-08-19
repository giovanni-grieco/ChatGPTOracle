package it.uniroma3.chatGPT.comando;

import it.uniroma3.chatGPT.utils.IntrospectionUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

public class ComandiFactory {
    public Comando makeComando(String comando) throws ClassNotFoundException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Class<?> classeComando =  Class.forName("it.uniroma3.chatGPT.comando.comandi."+convertiStringToComando(comando));
        Constructor<?> costruttoreComando = classeComando.getConstructors()[0];
        return (Comando) costruttoreComando.newInstance();
    }

    private static String convertiStringToComando(String input){
        String[] inputSplitted = input.split(" ");
        StringBuilder comando = new StringBuilder(inputSplitted[0].substring(0, 1).toUpperCase() + inputSplitted[0].substring(1));
        for(int i = 1; i < inputSplitted.length; i++){
            comando.append(inputSplitted[i].substring(0, 1).toUpperCase()).append(inputSplitted[i].substring(1));
        }
        return comando.toString();
    }

    private static String convertiComandoToString(String nomeClasse){
        String[] inputSplitted = nomeClasse.split("(?=\\p{Upper})");
        StringBuilder comando = new StringBuilder(inputSplitted[0].toLowerCase());
        for(int i = 1; i < inputSplitted.length; i++){
            comando.append(" ").append(inputSplitted[i].toLowerCase());
        }
        return comando.toString();
    }

    public String getComandi() {
        List<Class<?>> classiComando = IntrospectionUtils.getClassesInPackage("it.uniroma3.chatGPT.comando.comandi");
        StringBuilder sb = new StringBuilder();
        for(int i =0;i<classiComando.size()-1;i++){
            Class<?> c = classiComando.get(i);
            sb.append(convertiComandoToString(c.getSimpleName())).append(", ");
        }
        sb.append(convertiComandoToString(classiComando.get(classiComando.size()-1).getSimpleName()));
        return sb.toString();
    }
}
