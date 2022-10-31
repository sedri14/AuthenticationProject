package Controllers;

import com.google.gson.Gson;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

class UserRepository {
    //cache - load all files when app starts
    //for every read - read from cache
    //for every write - write to files and update cache
    private static volatile UserRepository userRepo;
    Map<String, User> usersCache;   //email as key

    static UserRepository getInstance() {

        UserRepository result = userRepo;

        if (result == null) {
            synchronized (AuthenticationService.class) {
                result = userRepo;
                if (result == null) {
                    userRepo = result = new UserRepository();
                }
            }
        }
        return result;
    }

    private UserRepository() {

        usersCache = new HashMap<>();
        loadAllUsersToCache(new File("src\\main\\java\\Controllers\\users"));
    }

    User readFromCache(String email){
        return usersCache.get(email);
    }

    private void loadAllUsersToCache(File folder) {
        for (File fileEntry: folder.listFiles()) {
            if (!fileEntry.isDirectory()) {
                if (Utils.isJsonFile(fileEntry)) {
                    User user = readFromFile(fileEntry.getAbsolutePath());
                    usersCache.put(user.getEmail(),user);
                }
            }
        }
    }

    void deleteFile(String path) {
        File file = new File("src\\main\\java\\Controllers\\users\\" + path);
        boolean b= file.delete();
    }

    void writeToFile(String fileName, User user) throws IOException {
        Gson gson = new Gson();
        try (FileWriter fw = new FileWriter("src\\main\\java\\Controllers\\users\\" + fileName)) {
            String userJson = gson.toJson(user);
            fw.write(userJson);
            usersCache.put(user.getEmail(), user);
        } catch (IOException e) {
            throw new IOException("cant write to new file to update");
        }
    }

    private User readFromFile(String fileName) {

        User readUser = null;
        try (FileReader fr = new FileReader(fileName)) {
            Gson gson = new Gson();
            readUser = gson.fromJson(fr, User.class);
            usersCache.put(readUser.getEmail(), readUser);
        } catch(FileNotFoundException e) {
            throw new RuntimeException("file not found");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return readUser;
    }
}
