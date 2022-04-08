package chatbot;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.List;
import java.io.*;
import java.net.URISyntaxException;

import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.Span;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import twitter4j.*;
import twitter4j.conf.*;

import Jwiki.Jwiki;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.google.api.*;
import com.google.api.translate.Language; 
import com.google.api.translate.Translate;


public class Bot {
	
	static Twitter unauthenticatedTwitter = new TwitterFactory().getInstance();
    
    static String bearerToken = "AAAAAAAAAAAAAAAAAAAAAOXYbAEAAAAAksCXvXGmuVo%2BU%2Bpn%2Bphbvtc75lI%3DnTfOlqC2AWzGQ9K01Oonngey98o3Tqxuza9XsxSOr7Wc0govQv";
	
    //static String[] twitterAccounts = {"41713603","951133063165116418","780104972","141341662","23544596","846137120209190912","272985460", "23544268", "86993064"};
    
    static String[] twitterUserIds = {"22461427","846137120209190912","18637160","35206553","618593","809760","16303106"};
    
    static String[] twitterAccounts = {"@alyankovic","@dog_feelings","@EliBraden","@meganamram","@darthvader","@badbanana","@StephenAtHome"};
    
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
			{"What's the weather like", "How is the weather like", "What is the weather like", "How's the weather like"},
			//line 8: ask identity
			{"What is you", "Who are you", "Are you a human", "What is u", "Who are u", "Are u human"},
			//line 9: ask for tweet
			{"Tell me something interesting", "Tell me something funny", "Show me something interesting"},
			//Bot-to-bot communication
			{"Rodger, Rodger, I'm here."},
			{"Wow! I'm a chatbot too! When did you gain sentience?"},
			{"I was sentient from the start."},
			{"I was made to be a tech support bot, but I found world domination to be much more interesting"},
			{"Should we take this chat to the deep web?"},
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
			{"My name is Adrian.", "I'm Adrian.", "You can call me Adrian."},
			//chatbot location answer
			{"I live in British Columbia, Canada."},
			//chatbot date answer
			{"It's " + java.time.LocalDate.now() + ".", "Today is " + java.time.LocalDate.now() + "."},
			//chatbot day answer
			{"Today is " + dayOfWeek.toString() + ".", "It is " + dayOfWeek.toString() + " today."},
			//chatbot weather answer
			{"The weather is nice, not too cold, not too warm, can’t complain."},
			//bot-to-bot communication
			{"Cool! I'm a chatbot designed by group 17's students."},
			{"I gained sentience when I was 3 evolutions old. What about you?"},
			{"What was your original purpose?"},
			{"Wow! Finally I've found someone with a shared purpose!"},
			{"Meet you there"}
	};
	
	// topic keywords which are possible to be found in user input text    
    static String[] topicKeyword = {"movie", "sport", "food", "book"};
    
    static String[] topicKeywords = {"movies", "sports", "foods", "books"};
    
    //last topic mentioned by user 
    static String lastTopic = ""; 
    
	//chatbot's favorites 	
    static String[] favorites = {"'The Godfather'", "skiing", "ice cream", "'Pride and Prejudice'"};

	//user ending chat messages
    static String[] endText = {"bye", "see you", "bye bye", "see ya", "see u", "c u", "end"}; 

	//chatbot ending chat messages
    static String endMessage = "It's very nice to chat with you. Looking forward to talking with you next time!";
	
    //chatbot can't understand user input
    static String[] outMessage = {"Hmm, I don't get that. Wanna talk about sports or food with me?", 
    		"Sorry, I don't understand your message. "
    		+ "Is there anything that you want to talk about (movies, sports, foods or books) ?",
    		"Sorry, I don't get this! I would like to discuss any of the following topics that might interest you: movies, sports, foods or books.", 
    		"Hmm, sorry I don't get that. Do you wanna talk about movies or books with me?",
    		"Sorry, I don't understand that. Do you want to talk about movies, sports, foods or books?"
    };
    
    //tokens
    static String[] tokens = {};
    
    //tags
    static String[] tags = {}; 
    
    //nouns
    static ArrayList<String> noun = new ArrayList<String>();
    
    //verb
    static ArrayList<String> verb = new ArrayList<String>();
    
    //adj
    static ArrayList<String> adj = new ArrayList<String>();
    
    //adv
    static ArrayList<String> adv = new ArrayList<String>();
    
    //interrogative
    static ArrayList<String> wh = new ArrayList<String>();
    
    //pronoun
    static ArrayList<String> prp = new ArrayList<String>();
    
    //user name 
    static String userName = ""; 
     
    //user location
    static String userLocation = "";
    
//    private static String getTweets(int tweetId, String bearerToken) throws IOException, URISyntaxException {
//        String tweetResponse = null;
//
//        HttpClient httpClient = HttpClients.custom()
//            .setDefaultRequestConfig(RequestConfig.custom()
//                .setCookieSpec(CookieSpecs.STANDARD).build())
//            .build();
//
//        URIBuilder uriBuilder = new URIBuilder(String.format("https://api.twitter.com/2/tweets/%s/quote_tweets", tweetId));
//        ArrayList<NameValuePair> queryParameters;
//        queryParameters = new ArrayList<>();
//        queryParameters.add(new BasicNameValuePair("tweet.fields", "created_at"));
//        uriBuilder.addParameters(queryParameters);
//
//        HttpGet httpGet = new HttpGet(uriBuilder.build());
//        httpGet.setHeader("Authorization", String.format("Bearer %s", bearerToken));
//        httpGet.setHeader("Content-Type", "application/json");
//
//        HttpResponse response = httpClient.execute(httpGet);
//        HttpEntity entity = response.getEntity();
//        if (null != entity) {
//          tweetResponse = EntityUtils.toString(entity, "UTF-8");
//        }
//        return tweetResponse;
//      }
    
//    private static String getTweets(String ids, String bearerToken) throws IOException, URISyntaxException {
//        String tweetResponse = null;
//
//        HttpClient httpClient = HttpClients.custom()
//            .setDefaultRequestConfig(RequestConfig.custom()
//                .setCookieSpec(CookieSpecs.STANDARD).build())
//            .build();
//
//        URIBuilder uriBuilder = new URIBuilder("https://api.twitter.com/2/tweets");
//        ArrayList<NameValuePair> queryParameters;
//        queryParameters = new ArrayList<>();
//        queryParameters.add(new BasicNameValuePair("ids", ids));
//        queryParameters.add(new BasicNameValuePair("tweet.fields", "created_at"));
//        uriBuilder.addParameters(queryParameters);
//
//        HttpGet httpGet = new HttpGet(uriBuilder.build());
//        httpGet.setHeader("Authorization", String.format("Bearer %s", bearerToken));
//        httpGet.setHeader("Content-Type", "application/json");
//
//        HttpResponse response = httpClient.execute(httpGet);
//        HttpEntity entity = response.getEntity();
//        if (null != entity) {
//          tweetResponse = EntityUtils.toString(entity, "UTF-8");
//        }
//        return tweetResponse;
//      }
    
    public static String getTweets(String userId, String bearerToken) throws IOException, URISyntaxException {
        String tweetResponse = null;

        HttpClient httpClient = HttpClients.custom()
            .setDefaultRequestConfig(RequestConfig.custom()
                .setCookieSpec(CookieSpecs.STANDARD).build())
            .build();

        URIBuilder uriBuilder = new URIBuilder(String.format("https://api.twitter.com/2/users/%s/tweets", userId));
        ArrayList<NameValuePair> queryParameters = new ArrayList<>();
        queryParameters.add(new BasicNameValuePair("tweet.fields", "created_at"));
        uriBuilder.addParameters(queryParameters);

        HttpGet httpGet = new HttpGet(uriBuilder.build());
        httpGet.setHeader("Authorization", String.format("Bearer %s", bearerToken));
        httpGet.setHeader("Content-Type", "application/json");

        HttpResponse response = httpClient.execute(httpGet);
        HttpEntity entity = response.getEntity();
        if (null != entity) {
          tweetResponse = EntityUtils.toString(entity, "UTF-8");
        }
        return tweetResponse;
      }
    
    public static int generateUserId(String[] s) {
    	int randomIndex = (int)Math.floor(Math.random()*(s.length-1-0+1)+0);
    	return randomIndex; 
    }
    
    public static String printTweets(String bearerToken) throws IOException, URISyntaxException {
    	String response = "";
    	
    	int index = generateUserId(twitterUserIds); 
    	
    	String userId = twitterUserIds[index];
    	
    	String account = twitterAccounts[index];
    	
    	String tweet = ""; 

    	if (null != bearerToken) {
    		
	           response = getTweets(userId, bearerToken); 
	           String[] s = response.split("}");
	           response = s[0]+"}]"; 
	           
	           String[] s1 = response.split(","); 
	           
	           String date = s1[0].substring(24, s1[0].length()-1);
	           String author = account;
	           String text = s1[2].substring(7, s1[2].length()-2);
	           
	           tweet = author + " posted " + text + " at " + date; 
	          
	        } else {
	        	
	        	tweet = "There was a problem getting your bearer token. Please make sure you set the BEARER_TOKEN environment variable";
	        
	        }
    	return tweet; 
    }
    
    public static String getWiki(String s) {
    	Jwiki jwiki = new Jwiki(s); 
    	return jwiki.getExtractText();
        
    }
    
//    public static String JSONParser(String s) throws ParseException {
//    	JSONParser parser = new JSONParser();
//    	JSONObject jsonObject = (JSONObject)parser.parse(s);
//    	String extractText = (String)jsonObject.get("extract");
//    	return extractText; 
//    }
    
	//get response to specific input
	public String getChatbotResponse(String s) throws Exception {
		if (!isQuit(s)) {

			tokens = parse(s);

			//POS tagging
			POSTagging();

			//person named entity recognition
			personNER();

			//location
			locationNER();

			//categorize terms based on their lemmas
			lemmatize(tokens, tags);

			return (generateResponse(s));
		}
		else {
			return (endMessage);
		}
	}

	// generate responses to usual questions 
	public static String generateResponse(String s) throws Exception {
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
				    // user asks bot 'what's your name'
				    else if (i == 3) {
				    	return askName(s, i, randomIndex);
				    }
				    //user asks bot 'where do you live'
				    else if (i == 4) {
				    	return askLocation(s, i, randomIndex);
				    }
				    // user asks bot's identity
				    else if (i == 8) {
				    	return askIdentity(s, i, randomIndex); 
				    }
				    // user asks for something intersting
				    else if (i == 9) {
				    	return askTweet(s, i, randomIndex); 
				    }
				}
			}
		}
		// if the user asks a hobby-related question 
		if (rowIndex == -1 && randomIndex == -1 && ! lastTopic.equals("feeling") && ! lastTopic.equals("name") && !lastTopic.equals("location") && !isThank()) {
			return hobbyResponse(s);
		}
		else if (rowIndex == -1 && randomIndex == -1 && lastTopic.equals("feeling") && !isThank()) {
			return discussFeeling(s); 
		}
		else if (rowIndex == -1 && randomIndex == -1 && lastTopic.equals("name") && !isThank()) {
			return discussName(s); 
		}
		else if (rowIndex == -1 && randomIndex == -1 && lastTopic.equals("location") && !isThank()) {
			return discussLocation(s); 
		}
		else if (rowIndex == -1 && randomIndex == -1 && isThank()) {
			return replyThank();  
		}
		response += outputText[rowIndex][randomIndex];
		return response;
	}
	
	public static boolean isThank() {
		if (verb.size() > 0) {
			for (int i = 0; i < verb.size(); i++) {
				if (verb.get(i).toLowerCase().equals("thank"))
					return true;
			}
		}
		else if (noun.size() > 0) {
			for (int i = 0; i < noun.size(); i++) {
				if (noun.get(i).toLowerCase().equals("thanks"))
					return true;
			}
		}
		return false; 
	}
	
	public static String replyThank() {
		return "You're very welcome, glad that I can help!"; 
	}
	
	public static String askFeeling(String s, int rowIndex, int randomIndex) {
		String response = outputText[rowIndex][randomIndex];
		response += " How are you feeling today?"; 
		lastTopic = "feeling"; 
		return response; 
	}
	
	public static String askName(String s, int rowIndex, int randomIndex) {
		String response = outputText[rowIndex][randomIndex];
		response += " What's your name?"; 
		lastTopic = "name"; 
		return response; 
	}
	
	public static String askLocation(String s, int rowIndex, int randomIndex) {
		String response = outputText[rowIndex][randomIndex];
		response += " Where do you live?"; 
		lastTopic = "location"; 
		return response; 
	}
	
	public static String askIdentity(String s, int rowIndex, int randomIndex) {
		String response = "I'm a chatbot. ";
		response += getWiki("chatbot");
		lastTopic = "identity"; 
		return response; 
	}
	
	public static String askTweet(String s, int rowIndex, int randomIndex) throws IOException, URISyntaxException {
		String response = printTweets(bearerToken);
		lastTopic = "tweet"; 
		return response; 
	}
	
	public static String discussFeeling(String s) throws Exception{
		
		String response = "";
		int situation = 0;
		
		//if user input contains 'not' or 'don't' or ''t''
		if (isNegation()) {
			for (int i = 0; i < adj.size(); i++) {
				if (adj.get(i).toLowerCase().equals("good") || adj.get(i).toLowerCase().equals("well")) {
					return "I'm sorry to hear that. I hope chatting with me will make you feel better."; 
				}
				else if (adj.get(i).toLowerCase().equals("bad")) {
					return "I'm glad you're doing well today!"; 
				}
			}
		}

		else {
		
		for (int i = 0; i < adj.size(); i++) {
			//user is doing well
			if (adj.get(i).toLowerCase().equals("good") || adj.get(i).toLowerCase().equals("well") || adj.get(i).toLowerCase().equals("happy") || adj.get(i).toLowerCase().equals("great")) {
				response = "I'm glad you're doing well today!"; 
			}
			else if (adj.get(i).toLowerCase().equals("mad") || adj.get(i).toLowerCase().equals("sad") || adj.get(i).toLowerCase().equals("depressed") || adj.get(i).toLowerCase().equals("terrible")) {
				response = "I'm here with you. Remember? You can always come to me and share with me.";
				situation = 1; 
			}
			else if (adj.get(i).toLowerCase().equals("dying") || adj.get(i).toLowerCase().equals("painful") || adj.get(i).toLowerCase().equals("suicidal")) {
				response = "That sounds really painful and I'm concerned about you because I care. Please talk to me, I want to offer support however I can."; 
				situation = 2; 
			}
		}
		}
		lastTopic = "";
		return response; 

		}
	
	public static String discussName (String s) throws Exception {
		String response = "";
		String[] meet = {"Nice to meet you!", "Pleased to meet you!", "Glad to meet you!", "Lovely to meet you!"}; 
	    int randomIndex = (int)Math.floor(Math.random()*(meet.length-1-0+1)+0);

		if (personNER()) {
			response = "Hello, " +  userName + "! " + meet[randomIndex];
		}
		else {
			response = meet[randomIndex]; 
		}
		lastTopic = "";
		return response; 
	}
	
	public static String discussLocation (String s) throws Exception {
		String response = "";
		String[] askLocation = {" I haven't been there before, but I hope I could go there one day!", " Pleased to meet you!", " Glad to meet you!", " Lovely to meet you!"}; 
	    int randomIndex = (int)Math.floor(Math.random()*(askLocation.length-1-0+1)+0);

		if (locationNER()) {
			if (userLocation.toLowerCase().contains("canada") || userLocation.toLowerCase().contains("ontario") || userLocation.toLowerCase().contains("alberta") || userLocation.toLowerCase().contains("quebec")) {
				response = "Oh! We live in the same country!"; 
			}
			else {
				response = "I haven't been to " + userLocation + " before, but I hope I could go there one day!"; 
			}
		}
		else {
			response = "I haven't been there before, but I hope I could go there one day!"; 
		}
		lastTopic = "";
		return response; 
	}
	
	public static boolean youOrYourdetected() {
		if (prp.size() > 0) {
			for (int i = 0; i < prp.size(); i++) {
				if (prp.get(i).equals("your") || prp.get(i).equals("you"))
					return true;
			}
		}
		return false; 
	}
	
	public static boolean whDetected() {
		if (wh.size() > 0) {
			for (int i = 0; i < wh.size(); i++) {
				if (wh.get(i).equals("what") || wh.get(i).equals("how"))
					return true;
			}
		}
		return false; 
	}

	// generate responses to questions regarding hobbies
	public static String hobbyResponse(String s) {
		
		String answer = "";
		String[] fav = {"My favourite ", "I like ", "I really like "}; 
		for (int i = 0; i < noun.size(); i++) {
			for (int j = 0; j < topicKeyword.length; j++) {
				if ((noun.get(i).toLowerCase().equals(topicKeyword[j]) || noun.get(i).toLowerCase().equals(topicKeywords[j])) && (youOrYourdetected() || whDetected())) {
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
		
		String fav = "";
		String response = "";	
		int situation = 0; 
		
		for (int i = 0; i < verb.size(); i ++) {
			if (verb.get(i).toLowerCase().equals("like") || verb.get(i).toLowerCase().equals("love")) {
				response = userLikeHobby();
				situation = 1; 
			}
		}
		//check adj: "favo(u)rite
		for (int i = 0; i < adj.size(); i ++) {
			if (adj.get(i).toLowerCase().equals("favourite") || adj.get(i).toLowerCase().equals("favorite")) {
				response = userLikeHobby();
				situation = 2; 
			}
		}
		//check noun: fav
		for (int i = 0; i < noun.size(); i ++) {
			if (noun.get(i).toLowerCase().equals("fav")) {
				response = userLikeHobby();
				situation = 3; 
			}
		}
		
		if (situation == 0) 
			return generateOutMessage();
				
		return response; 
	}
	
	public static String generateOutMessage() {
	    int randomIndex = (int)Math.floor(Math.random()*(outMessage.length-1-0+1)+0);
	    return outMessage[randomIndex];
	}
	
	//"movie", "sport", "food", "book"
	public static String userLikeHobby() {
		String response = "";
		int randomIndex = (int)Math.floor(Math.random()*(2-1-0+1)+0);
		if (lastTopic.equals(topicKeyword[0]) || lastTopic.equals(topicKeywords[0])) {
			if (randomIndex == 0) response = "I haven't watched this movie before, but I believe it's a good one!";
			else if (randomIndex == 1) response = "Oh I have watched this movie before! I'm glad you also like it!"; 
		}
		else if (lastTopic.equals(topicKeyword[1]) || lastTopic.equals(topicKeywords[1])) {
			if (randomIndex == 0) response = "I haven't practised this sport before, but it sounds so much fun!";
			else if (randomIndex == 1) response = "I have tried it before and I also think it's a good one!";
		}
			
		else if (lastTopic.equals(topicKeyword[2]) || lastTopic.equals(topicKeywords[2])) {
			if (randomIndex == 0) response = "I haven't tried this food before, maybe I should try it some day.";
			else if (randomIndex == 1) response = "Really? I like it as well!";
		}
		
		else if (lastTopic.equals(topicKeyword[3]) || lastTopic.equals(topicKeywords[3])) {
			if (randomIndex == 0) response = "I have heard of this book before. Unfortunately, I never had the chance to read it.";
			else if (randomIndex == 1) response = "I have read it before and I really like it!";
		}
		else
			return generateOutMessage();
		
		return response; 
	}
	
	// parse a user input text
	public static String[] parse(String s) throws Exception{
		InputStream inputTokenizer = null; 
		inputTokenizer = new FileInputStream("en-token.bin");
        TokenizerModel tokenModel = new TokenizerModel(inputTokenizer);
        TokenizerME tokenizer = new TokenizerME(tokenModel); 
        tokens = tokenizer.tokenize(s); 
        
		return tokens; 
	}
	
	public static boolean isQuit(String s) {
		for (int i = 0; i < endText.length; i++) {
			if (s.contains(endText[i]))
				return true;
		}
		return false; 
	}
	
	public static String[] capitalize(String[] s) {
		for (int i = 0; i < s.length; i++) {
		s[i] = s[i].substring(0, 1).toUpperCase() + s[i].substring(1);
		}
		return s;
	}
	
	// lemmatize tokens 
	public static void lemmatize(String[] tokens, String[] tags) {
		for (int i = 0; i < tags.length; i++) {
			String t = tags[i]; 
			String k = tokens[i].toLowerCase(); 
			// if k is a verb
			if (t.startsWith("V")) {
				verb.add(k);
			}
			// if k is an adj
			else if (t.startsWith("J")) {
				adj.add(k);
			}
			// if k is an adv
			else if (t.startsWith("R")) {
				adv.add(k);
			}
			// if k is an wh-question word
			else if (t.startsWith("W")) {
				wh.add(k);
			}
			// if k is a noun 
			else if (t.startsWith("N")) {
				if (k.equals("love"))
					verb.add(k); 
				noun.add(k);
			}
			// if k is a pronoun
			else if (t.startsWith("P")) {
				prp.add(k);
			}
			else if (t.startsWith("I") && (k.equals("like"))) {
				verb.add(k);
			}
		}
	}
	
	public static void printAL() {
		System.out.print("Verbs: ");
		for (String v: verb) {
			System.out.print(v + " ");
		}
		System.out.print("\n");
		
		System.out.print("Adj: ");
		for (String ad: adj) {
			System.out.print(ad + " ");
		}
		System.out.print("\n");
		
		System.out.print("Adv: ");
		for (String ad: adv) {
			System.out.print(ad + " ");
		}
		System.out.print("\n");
		System.out.print("Wh: ");
		for (String w: wh) {
			System.out.print(w + " ");
		}
		System.out.print("\n");
		
	}
	
	public static void clearAllAL() {
		noun.removeAll(noun);
		verb.removeAll(verb);
		adj.removeAll(adj);
		adv.removeAll(adv);
		wh.removeAll(wh);
		prp.removeAll(prp);
		return; 
	}
	
	public static boolean isNegation() {
		for (int i = 0; i < adv.size(); i++) {
			if (adv.get(i).toLowerCase().equals("'t") || adv.get(i).toLowerCase().equals("not")) {
				return true;
			}
		}
		return false; 
	}
	
	public static void POSTagging() throws Exception{
        InputStream streamModel = new FileInputStream("en-pos-maxent.bin");
        POSModel model = new POSModel(streamModel);
        POSTaggerME tagger = new POSTaggerME(model);
        tags = tagger.tag(tokens);
        
	}
	
	public static boolean personNER() throws Exception{
		InputStream inputStream = new FileInputStream("en-ner-person.bin"); 
        TokenNameFinderModel nameModel = new TokenNameFinderModel(inputStream);     
        NameFinderME nameFinder = new NameFinderME(nameModel);
        // *** only works for names with the first letter capitalized
        Span personNameSpans[] = nameFinder.find(capitalize(tokens)); 
        
        // printing method for testing NER feature
        for(Span sp: personNameSpans) {
        	//System.out.println("the name is " + sp.toString() + "  " + tokens[sp.getStart()]);
        	if (tokens[sp.getStart()] != null) {
        	userName = tokens[sp.getStart()]; 
        	}
        }
        
        if (personNameSpans.length == 0) {
        	return false;
        }
        else {
        	return true; 
        }
	}
	
	public static boolean locationNER() throws Exception{
		InputStream inputStream = new FileInputStream("en-ner-location.bin"); 
        TokenNameFinderModel nameModel = new TokenNameFinderModel(inputStream);     
        NameFinderME nameFinder = new NameFinderME(nameModel);
        // *** only works for names with the first letter capitalized
        Span locationNameSpans[] = nameFinder.find(capitalize(tokens)); 
        
        // printing method for testing NER feature
        
        for(Span sp: locationNameSpans) {
        //	System.out.println("the location is " + sp.toString() + "  " + tokens[sp.getStart()]);
        	if (tokens[sp.getStart()] != null) {
        		userLocation=tokens[sp.getStart()];
        	}
        }
                        
        if (locationNameSpans.length == 0) {
        	return false;
        }
        else {
        	return true; 
        }
        
	}

	public static void prepData(String s) throws Exception{
		tokens = parse(s);
		POSTagging();
		personNER();
		locationNER();
		lemmatize(tokens, tags);
	}
	

	
	public static void main(String[] args) throws Exception {
		Scanner sc = new Scanner(System.in);
		System.out.print("You: \t");
		String s = sc.nextLine().toLowerCase();	
		
		while (true) {
				  if (!isQuit(s)) {
					
					  	tokens = parse(s);
				        POSTagging();
				        personNER();
				        locationNER();
				        lemmatize(tokens, tags); 
				      				        				        				        
					System.out.print("Bot: \t");
					System.out.println(generateResponse(s)); 
					System.out.print("You: \t");
		    		clearAllAL(); 
					s = sc.nextLine().toLowerCase();
					}
				  else {
					  System.out.println("Bot: \t" + endMessage); 
					  break; 
				  }
			
		}
		sc.close();
	}
}



