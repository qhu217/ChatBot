package chatbot;
import java.util.Scanner;
import java.io.*;
import java.nio.charset.StandardCharsets;
import net.didion.jwnl.*;
import net.didion.jwnl.data.*;
import net.didion.jwnl.dictionary.Dictionary;
import opennlp.tools.*;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSSample;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;

import org.apache.commons.io.*;

class Bot {
	
	//WordNet dictionary 
	Dictionary dict = Dictionary.getInstance();
	
	// possible user input
	static String[][] inputText = {
			//standard greetings
			{"Hi", "Hello", "Hey", "Hola", "Ola"},
			//question greetings
			{"How are you", "What's up", "Sup", "How r you", "How r u", "How are u"},
			//age questions
			{"How old are you", "How old are u", "How old r you", "How old r u"},
			//name questions
			{"What is your name", "What's your name", "Do you have a name"},
			//location questions 
			{"Where do you live", "Where are you located", "Where do u live", "Where are u located", "Where r you located", "Where r u located"},
			//date questions
			{"What's the date today", "What is the date today"},
			//day questions
			{"What day is it today", "What is the day today", "What's the day today"},
			//weather questions
			{"What's the weather like", "How is the weather like", "What is the weather like", "How's the weather like"}

	};
	
    static java.time.DayOfWeek dayOfWeek = java.time.LocalDate.now().getDayOfWeek();

	// chatbot responses 
    static String[][] outputText = {
			//chatbot standard greetings
			{"Hello!", "Hey!", "Hi!"},
			//chatbot question greetings
			{"I'm good!", "I'm doing well!", "I'm doing great!"},
			//chatbot age answer
			{"I'm less than one year old."},
			//chatbot name answer
			{"My name is Alfrie.", "I'm Alfrie.", "You can call me Alfrie."},
			//chatbot location answer
			{"I live in British Columbia, Canada."},
			//chatbot date answer
			{"It's " + java.time.LocalDate.now() + ".", "Today is " + java.time.LocalDate.now() + "."},
			//chatbot day answer
			{"Today is " + dayOfWeek.toString() + ".", "It is " + dayOfWeek.toString() + " today."},
			//chatbot weather answer
			{"The weather is nice, not too cold, not too warm, can’t complain."}

	};
	
	// topic keywords which are possible to be found in user input text    
    static String[] topicKeyword = {"movie", "sport", "food", "book"};
    
    //last topic mentioned by user 
    static String lastTopic = ""; 
    
	//chatbot's favorites 	
    static String[] favorites = {"'The Godfather'", "skiing", "ice cream", "'Pride and Prejudice'"};

	//user ending chat messages
    static String[] endText = {"bye", "see you", "bye bye", "see ya", "see u", "c u", "end"}; 

	//chatbot ending chat messages
    static String endMessage = "It's very nice to chat with you. Looking forward to talking with you next time!";
		
	// generate responses to usual questions 
	public static String generateResponse(String s) {
		int rowIndex = -1; 
		int randomIndex = -1; 
		String response = ""; 

		for (int i = 0; i < inputText.length; i++) {
			for (int j = 0; j < inputText[i].length; j++) {
				if (s.equals(inputText[i][j].toLowerCase()) || s.equals(inputText[i][j].toLowerCase()+"?") || s.equals(inputText[i][j].toLowerCase()+"!") || s.equals(inputText[i][j].toLowerCase()+".")) {
					rowIndex = i;
					// generate a random corresponding chatbot answer to the user input question
				    randomIndex = (int)Math.floor(Math.random()*(outputText[i].length-1-0+1)+0);
				    // user asks bot 'how are you'
				    if (i == 1) {
				    	return askFeeling(s, i, randomIndex); 
				    }	
				}
			}
		}
		// if the user asks a hobby-related question 
		if (rowIndex == -1 && randomIndex == -1 && ! lastTopic.equals("feeling")) {
			return hobbyResponse(s);
		}
		else if (rowIndex == -1 && randomIndex == -1 && lastTopic.equals("feeling")) {
			return discussFeeling(s); 
		}
		response += outputText[rowIndex][randomIndex];
		return response;
	}
	public static String askFeeling(String s, int rowIndex, int randomIndex) {
		String response = outputText[rowIndex][randomIndex];
		response += " How are you feeling today?"; 
		lastTopic = "feeling"; 
		return response; 
	}
	
	public static String discussFeeling(String s) {
		String[] tokens = parse(s);
		String response = "";
		String keywords = "";
		int situation = 0;
		for (String t: tokens) {
			if (t.toLowerCase().equals("not")||t.toLowerCase().equals("don't")||t.toLowerCase().equals("good")||t.toLowerCase().equals("well")) {
				keywords+=t;
			}
		}
		if (keywords.contains("not")||keywords.contains("don't")&&keywords.contains("good")||keywords.contains("well")) {
			return "I'm sorry to hear that. I hope chatting with me will make you feel better."; 
		}
		else {
		for (int i = 0; i < tokens.length; i++) {
			for (int j = 1; j < tokens.length; j++) {
						if (tokens[j] .toLowerCase().equals("good") || tokens[j] .toLowerCase().equals("well") || tokens[j] .toLowerCase().equals("happy")) {
							response = "I'm glad you're doing well today."; 
							}
						else if (tokens[i].toLowerCase().equals("terrible") || tokens[i].toLowerCase().equals("depressed") ||tokens[i].toLowerCase().equals("mad") || tokens[i].toLowerCase().equals("sad")) {
							response = "I'm here with you. Remeber? You can always come to me and share with me."; 
							situation = 1;
							}
						else if (tokens[i].toLowerCase().equals("dying") || tokens[i].toLowerCase().equals("suicidal") || tokens[i].toLowerCase().equals("painful")) {
							response = "That sounds really painful and I'm concerned about you because I care. Please talk to me, I want to offer support however I can."; 
							situation = 2; 
							}
					}

				}
		}
		return response; 

		}
	

	// generate responses to questions regarding hobbies
	public static String hobbyResponse(String s) {
		String[] tokens = parse(s);
		String answer = "";
		String[] fav = {"My favourite ", "I like ", "I really like "}; 
		for (int i = 0; i < tokens.length; i++) {
			for (int j = 0; j < topicKeyword.length; j++) {
				if (tokens[i].contains(topicKeyword[j])) {
					lastTopic = topicKeyword[j];
					int randomIndex = (int)Math.floor(Math.random()*(fav.length-1-0+1)+0);
					if (randomIndex == 0)
						answer = fav[0] + topicKeyword[j] + " is " + favorites[j] + "." + " Which " + topicKeyword[j] + " do you like the most?"; 
					else if (randomIndex == 1)
						answer = fav[1] + favorites[j] + " the most." + " Which " + topicKeyword[j] + " do you like the most?"; 
					else if (randomIndex == 2)
						answer = fav[2] + favorites[j]+ "." + " Which " + topicKeyword[j] + " do you like the most?"; 
				}
			}
		}
		if (answer.equals(""))
			answer = checkUserHobby(s); 
		
		return answer; 
	}
	// check if a user input includes some hobbies
	public static String checkUserHobby(String s) {
		String[] tokens = parse(s);
		String fav = "";
		String response = "";
		for (int i = 0; i < tokens.length; i++) {
			int randomIndex = (int)Math.floor(Math.random()*(2-1-0+1)+0);
			if (tokens[i].toLowerCase().equals("like") || tokens[i].toLowerCase().equals("love")) {
				if (i+1<tokens.length)
					fav+=tokens[i+1].toLowerCase();
			}
			else if (tokens[i].toLowerCase().equals("favourite") || tokens[i].toLowerCase().equals("fav")) {
				if (i+2<tokens.length) {
					fav+=tokens[i+2].toLowerCase();
				}
			}
			if (!fav.equals(favorites[0]) || !fav.equals(favorites[1]) || !fav.equals(favorites[2]) || !fav.equals(favorites[3])) {
				if (lastTopic.equals(topicKeyword[0])) {
					if (randomIndex == 0) response = "I haven't watched this movie before, but I believe it's a good one!";
					else if (randomIndex == 1) response = "Oh I have watched this movie before! I'm glad you also like it!"; 
				}
					
				else if (lastTopic.equals(topicKeyword[1])) {
					if (randomIndex == 0) response = "I haven't practised this sport before, but it sounds so much fun!";
					else if (randomIndex == 1) response = "I have tried it before and I also think it's a good one!";
				}
					
				else if (lastTopic.equals(topicKeyword[2])) {
					if (randomIndex == 0) response = "I haven't tried this food before, maybe I should try it some day.";
					else if (randomIndex == 1) response = "Really? I like it as well!";
				}
				
				else if (lastTopic.equals(topicKeyword[3])) {
					if (randomIndex == 0) response = "I have heard of this book before. Unfortunately, I never had the chance to read it.";
					else if (randomIndex == 1) response = "I have read it before and I really like it!";
				}
			}
			else if (fav.equals(favorites[0]) || fav.equals(favorites[1]) || fav.equals(favorites[2]) || fav.equals(favorites[3])){
				response = "Really? I'm so glad that you also like it!"; 
			}
		}
		if (fav.equals("")) 
			response = "Sorry, I don't understand your message. Is there anything that you want to talk about (movies, sports, foods or books) ?"; 
		return response; 
	}

	// parse a user input text
	public static String[] parse(String s) {
		String[] tokens = s.split("\\s+");
		return tokens; 
	}
	
	public static void main(String[] args) throws Exception {
		Scanner sc = new Scanner(System.in);
		System.out.print("You: \t");
		String s = sc.nextLine().toLowerCase();	
	    	
		InputStream tokenModelIn = null;
        InputStream posModelIn = null;
		while (true) {
				  if (!s.equals("bye")) {

			            String tokens[] = parse(s);			            
			            
			            // Parts-Of-Speech Tagging
			            // reading parts-of-speech model to a stream 
			            posModelIn = new FileInputStream("en-pos-maxent.bin");
			            // loading the parts-of-speech model from stream
			            POSModel posModel = new POSModel(posModelIn);
			            // initializing the parts-of-speech tagger with model 
			            POSTaggerME posTagger = new POSTaggerME(posModel);
			            // Tagger tagging the tokens
			            String tags[] = posTagger.tag(tokens);
			            
			            
			            // Getting the probabilities of the tags given to the tokens
			            double probs[] = posTagger.probs();
			             
			            System.out.println("Token\t:\tTag\t:\tProbability\n---------------------------------------------");
			            for(int i=0;i<tokens.length;i++){
			                System.out.println(tokens[i]+"\t:\t"+tags[i]+"\t:\t"+probs[i]);
			            }
					  
					  
					System.out.print("Bot: \t");
					System.out.println(generateResponse(s)); 
					System.out.print("You: \t");
					s = sc.nextLine().toLowerCase();
					}
				  else {
					  System.out.println("Bot: \t" + endMessage); 
					  break; 
				  }
			
		}
		
	}

}

