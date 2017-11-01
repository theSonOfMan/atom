package ru.atom.chat;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

@Controller
@RequestMapping("chat")
public class ChatController {
    private static final Logger log = LogManager.getLogger(ChatController.class);

    private Deque<String> messages = new ConcurrentLinkedDeque<>();
    private Set<String> online = new HashSet<>();

    /**
     * curl -X POST -i localhost:8080/chat/login -d "name=I_AM_STUPID"
     */
    @RequestMapping(
            path = "login",
            method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<String> login(@RequestParam("name") String name) {
        if (name == null || name.isEmpty()) {
            return new ResponseEntity<>("No name provided", HttpStatus.BAD_REQUEST);
        }
        if (!online.add(name)) {
            return new ResponseEntity<>("Already logged in", HttpStatus.BAD_REQUEST);
        }
        messages.addFirst("<font face=\"Helvetica\" color=\"green\">[" + name + "]</font><font face=\"Helvetica\"> is online</font>");
        log.info(name + " logged in");
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * curl -X POST -i localhost:8080/chat/logout -d "name=I_AM_STUPID"
     */
    @RequestMapping(
            path = "logout",
            method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<String> logout(@RequestParam("name") String name) {
        log.info("Entered logout method");
        if (name == null || name.isEmpty()) {
            return new ResponseEntity<>("No name provided", HttpStatus.BAD_REQUEST);
        }
        if (!online.remove(name)) {
            return new ResponseEntity<>("No logged user with matching name", HttpStatus.BAD_REQUEST);
        }
        messages.addFirst("<font face=\"Helvetica\" color=\"red\">[" + name + "]</font><font face=\"Helvetica\"> is offline</font>");
        log.info(name + " logged out");
        return new ResponseEntity<>(HttpStatus.OK);
    }


    /**
     * curl -i localhost:8080/chat/online
     */
    @RequestMapping(
            path = "online",
            method = RequestMethod.GET,
            produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity online() {
        Iterator<String> itr = online.iterator();
        while (itr.hasNext()) {
            messages.addFirst("[" + itr.next() + "]");
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }


    /**
     * curl -X POST -i localhost:8080/chat/say -d "name=I_AM_STUPID&msg=Hello everyone in this chat"
     */
    @RequestMapping(
            path = "say",
            method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<String> say(@RequestParam("name") String name, @RequestParam("msg") String msg) {
        Date date = new Date();
        if (!online.contains(name)) {
            return new ResponseEntity<>("Not logined", HttpStatus.UNAUTHORIZED);
        }
        if (msg == null) {
            return new ResponseEntity<>("No message provided", HttpStatus.BAD_REQUEST);
        }
        if (msg.length() > 140) {
            return new ResponseEntity<>("Too long message", HttpStatus.BAD_REQUEST);
        }
        SimpleDateFormat formatForDateNow = new SimpleDateFormat("HH:mm:ss ");
        log.info(formatForDateNow.format(date) + "[" + name + "]: " + msg);
        messages.addFirst(formatForDateNow.format(date) +"[" + name + "]: " + msg);
        try {
            FileWriter writer = new FileWriter("ChatHistory.txt", true);
            for (String line : messages) {
                writer.write(line);
                writer.write(System.getProperty("line.separator"));
            }
            writer.flush();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        return new ResponseEntity<>(HttpStatus.OK);
    }


    /**
     * curl -i localhost:8080/hello/chat
     */
    @RequestMapping(
            path = "chat",
            method = RequestMethod.GET,
            produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> chat() {
        return new ResponseEntity<>(messages.stream()
                .map(Object::toString)
                .collect(Collectors.joining("\n")),
                HttpStatus.OK);
    }
}
