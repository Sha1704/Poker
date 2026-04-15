package app;
import java.io.*;
import java.util.*;
import java.security.*;
import java.nio.charset.StandardCharsets;

/**
 * Implementation to authenticate the account
 * @author Dariya
 */
public class Authentication {
    private static final int MAX_ATTEMPTS = 3;
    public static final String QUESTION = "What was the name of your first stuffed animal?";
    private static final String FILE = "user.txt";

    /** 
     * CWE-289: Authentication Bypass by Alternate Name
     * Method to normalize answer or username so all can be match
     * dariya = Dariya = DARIYA
     * 
     * @param input the input string
     * @return normalized string
     */
    private String clean(String input){
        if(input == null){
            return "";
        }
        String trimmed = input.trim();
        String lowercased = trimmed.toLowerCase(Locale.ROOT);
        return lowercased;
    }

    /**
     * CWE-303: Incorrect Implementation of Authentication Algorithm
     * CWE-836: Use of Password Hash Instead of Password for Authentication
     * Generate a SHA-256 hash with salt for server
     * 
     * @param value the value to hash like password or answer
     * @param salt the salt value
     * @return hexadecimal string of hashed value
     * @throws NoSuchAlgorithmException if SHA-256 is unavailable
     */
    private String hash(String value, String salt) throws NoSuchAlgorithmException{
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        // combine salt with value and convert to bytes
        String saltVal = salt + value;
        byte[] hashedBytes = md.digest(saltVal.getBytes(StandardCharsets.UTF_8));
        // convert bytes to string
        StringBuilder hexResult = new StringBuilder();
        for(byte hashByte : hashedBytes){
            String hexadecimal = String.format("%02x", hashByte);
            hexResult.append(hexadecimal);
        }
        return hexResult.toString();
    }

    /**
     * Generates a random salt using SecureRandom
     * 
     * @return hexadecimal string representing the salt
     */
    private String generateSalt(){
        // create byte array for salt
        byte[] saltBytes = new byte[16];
        // fill with secure random values
        SecureRandom random = new SecureRandom();
        random.nextBytes(saltBytes);
        StringBuilder hexSalt = new StringBuilder();
        for(byte saltByte : saltBytes){
            String hexadecimal = String.format("%02x", saltByte);
            hexSalt.append(hexadecimal);
        }
        return hexSalt.toString();
    }

    /**
     * Method to find a user in the file by username
     * 
     * @param username normalized username
     * @return user data array if found, null otherwise
     * @throws IOException if fail to read file
     */
    private String[] findUser(String username) throws IOException{
        File userFile = new File(FILE);
        if(!userFile.exists()) {
            return null;
        }
        try(BufferedReader br = new BufferedReader(new FileReader(userFile))){
            String line;
            // read file line by line
            while((line = br.readLine())!=null){
                String[] parts = line.split("\\|", 6);
                // check if format is correct and username matches
                if(parts.length == 6 && parts[0].equals(username)){
                    return parts;
                }
            }
        }
        // no matching user found
        return null;
    }

    /**
     * Method to save updated user data back to the file
     * 
     * @param username the username to update
     * @param updatedUser updated user data fields
     * @throws IOException if fail to run the file
     */
    private void saveUser(String username, String[] updatedUser) throws IOException{
        File userFile = new File(FILE);
        List<String> lines = new ArrayList<>();
        try(BufferedReader br = new BufferedReader(new FileReader(userFile))){
            String line;
            while((line = br.readLine())!=null){
                String[] parts = line.split("\\|", 6);
                // check if this is the user we want to update
                boolean isUser = parts.length == 6 && parts[0].equals(username);
                if(isUser){
                    // replace with updated user data
                    String updatedLine = String.join("|", updatedUser);
                    lines.add(updatedLine);
                } else {
                    lines.add(line);
                }
            }
        }
        // write updated data back to file
        try(BufferedWriter bw = new BufferedWriter(new FileWriter(userFile, false))){
            for(String currLine : lines){
                bw.write(currLine);
                bw.newLine();
            }
        }
    }

    /**
     * Method to register a new user 
     * 
     * @param username user's username
     * @param password user's password (must be at least 8 characters)
     * @param answer security question answer
     * @return true if register successfully, false otherwise
     * @throws Exception if fail to operate file or hashing
     */
    public boolean register(String username, String password, String answer) throws Exception{
        username = clean(username);
        // simple validate password
        if(password == null || password.isBlank() || password.length() < 8){
            System.out.println("Password must be at least 8 characters.");
            return false;
        }
        if(findUser(username)!=null){
            System.out.println("Username already taken!");
            return false;
        }
        String salt = generateSalt();
        // CWE-836 & CWE-303
        // hash password and answer here
        String line = username + "|" + salt + "|" + hash(password, salt) + "|" 
                    + hash(clean(answer), salt) + "|0|false";
        try(BufferedWriter bw = new BufferedWriter(new FileWriter(FILE, true))){
            bw.write(line);
            bw.newLine();
        }
        System.out.println("Account created successfully!");
        return true;
    }

    /**
     * Method to login by authenticate a user and locked the acc after many attempts
     * 
     * @param username user's username
     * @param password user's password
     * @param answer security answer
     * @return true if login is successful, otherwise false
     * @throws Exception if fail to operate file or hashing
     */
    public boolean login(String username, String password, String answer) throws Exception {
        username = clean(username);
        String[] user = findUser(username);
        if(user == null){
            System.out.println("User could not be found");
            return false;
        }
        // CWE-307: Improper Restriction of Excessive Authentication Attempts
        // locked account after 3 attempts 
        if(user[5].equals("true")){
            System.out.println("Account is locked after too many failed attempts.");
            return false;
        }
        String salt = user[1];
        // hash what user typed and then compare (CWE-836 & CWE-303)
        boolean passwordCorrect = MessageDigest.isEqual(
            hash(password, salt).getBytes(StandardCharsets.UTF_8), user[2].getBytes(StandardCharsets.UTF_8));
        // CWE-308: Use of Single-factor Authentication
        boolean answerCorrect = MessageDigest.isEqual(
            hash(clean(answer), salt).getBytes(StandardCharsets.UTF_8), user[3].getBytes(StandardCharsets.UTF_8));
        if(!passwordCorrect||!answerCorrect){
            int failed = Integer.parseInt(user[4]) + 1;
            user[4] = String.valueOf(failed);
            // CWE-307
            boolean isLocked = failed >= MAX_ATTEMPTS;
            if(isLocked){
                user[5] = "true";
            } else {
                user[5] = "false";
            }
            // save changes to file
            saveUser(username, user);
            System.out.println("Login failed. Attempt " + failed + " out of " + MAX_ATTEMPTS);
            return false;
        }
        // reset failed attempts on success
        user[4] = "0";
        user[5] = "false";
        saveUser(username, user);
        System.out.println("Login successfully! Welcome back, " + username);
        return true;
    }
}
