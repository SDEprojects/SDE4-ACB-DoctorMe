package com.doctorme.app;

import com.doctorme.GUI.GameGUI;
import com.doctorme.entities.*;
import com.doctorme.util.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Game {

/*
Controller for a quiz game -
current game has 12 locations, 60 questions, 5 questions per room
game can be expanded or changed by changing the question, location, and game text xml files
possibilities are endless, host your own trivia night :)
 */

    // FIELDS
    private String fileName; // Expansion - to allow user to choose different sets of questions or different rooms, also can be used to level up with insertion of more xml
    private String nodeNameXML; // same as above comment
    // available lists - questions, locations

    private final List<Badge> badges = new ArrayList<>(); // list of badges
    // access other classes
    private final QuestionGenerator qg = new QuestionGenerator();
    private final LocationGenerator lg = new LocationGenerator();
    private final Player currentPlayer = new Player();
    private final ConvertAnswer conAns = new ConvertAnswer();
    private final GameTextGenerator gtg = new GameTextGenerator();
    private final HashMap<String, Integer> categoryPoints = new HashMap<>();
    private int currentLevel;

    // START HERE
    public void startGame() {
        // instantiate and start the GUI, send in the welcome and instruction text from game text generator
        GameGUI gooey = new GameGUI(gtg.printWelcome(), gtg.printIntro(), gtg.printInstructions());
        gooey.updateHelpScreenText(gtg.printGameScreenHelpInstruction());

        setCurrentLevel(1);
        lg.bringLocations(); // set locations
        qg.bringQuestions(); // set questions
        setBadges(new BadgeGenerator().allBadges());

        while (!gooey.isEnteredGame()) {  //wait for player to exit initial setup, then set initial values

            try {
                Thread.sleep(100);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        }

        // INITIALIZE - first location and question in GUI
        // current location - randomized start, same map, changeable if fixed starter room preferred
        Location location = lg.startLocation();  // calls to location generator and returns a random room

        // initialize question and location fields for first display
        // STOCKS FIRST QUESTION - send in first location
        stockNextQuestion(gooey, location);
        // STOCKS FIRST LOCATION in GUI
        stockLocation(gooey, location);
        // SET SCORE IN GUI
        gooey.setCurrentScore(0);
        // update GUI
        gooey.guiUpdate();

        while (true) {
            // allows pause for code to enter if statement
            try {
                Thread.sleep(100);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }

            // if the player clicks the 'submit' button under questions
            if (gooey.isHasSubmittedAnswer()) {
                // if correct answer, remove question and assign points / badge
                if (gooey.hadCorrectAnswer()) {
                    qg.removeCorrAnsQuest();

                    // grab current question object, check type, and generate points and/or badge
                    String questType = qg.getCurrQ().getType().toLowerCase();

                    if(categoryPoints.containsKey(questType)){
                        categoryPoints.put(questType,categoryPoints.get(questType)+(currentLevel * 10));
                    }
                    else{
                        categoryPoints.put(questType, currentLevel * 10);
                    }

                    if(categoryPoints.get(questType)>=30){
                        if (awardBadge(questType)) gooey.setBadges(currentPlayer.getBadges());
                    }
                    setCurrentGameScore(getCurrentGameScore() + currentLevel * 10);
                    gooey.setCurrentScore(getCurrentGameScore());
                    gooey.setBadgeProgress(categoryPoints.get(questType) / (currentLevel * 10));

                    // if wrong answer
                }else{
                    gooey.setHasSubmittedAnswer(false);
                }

                // if user clicks 'next question'
            }else if (gooey.isReadyForNextQuestion()) {
                // set next Question object in GUI
                stockNextQuestion(gooey, location);
                // update GUI
                gooey.guiUpdate();

                // if user clicks on a location
            } else if (gooey.isWantsToChangeLocation()) {

                // retrieves location name from GUI button press - send String locationName to Location generator for next location to retrieve object location
                location = lg.nextLocation((gooey.getNextLocation()));

                // use new location to reset room and questions
                stockLocation(gooey, location);
                stockNextQuestion(gooey, location);
                if (categoryPoints.get(location.getType().toLowerCase()) != null){
                    gooey.setBadgeProgress(categoryPoints.get(location.getType().toLowerCase()) / (currentLevel * 10));
                }else{
                    gooey.setBadgeProgress(0);
                }
                gooey.guiUpdate();
            }
        }
    }

    // STOCK THE QUESTION OBJECT
    private void stockNextQuestion(GameGUI gooey, Location location) {
        Question currQ = qg.nextQuestion(location);
        gooey.updateQuestion(currQ.getQuestion());
        gooey.updateOptionA(currQ.getPossibleAnswers().get(0));
        gooey.updateOptionB(currQ.getPossibleAnswers().get(1));
        gooey.updateOptionC(currQ.getPossibleAnswers().get(2));
        gooey.updateOptionD(currQ.getPossibleAnswers().get(3));
        gooey.setCorrectAnswer(conAns.convertCorrectAns(currQ.getCorrectAnswer()));
        gooey.updateHintText(currQ.getHint());
    }

    // STOCK THE LOCATION OBJECT
    private void stockLocation(GameGUI gooey, Location location) {
        String currLocalDescrip = location.getDescription();
        String typeLocal = location.getType();
        gooey.updateCurrentLocation(location.getName());
        gooey.updateLocationDescription("Topic: " + typeLocal + "\n" + "View: " + currLocalDescrip);
        gooey.updateNextLocations(location.getRoomLeadTo());
    }

    /* QUESTION METHODS  are all in the Question Generator
    LOCATION METHODS are all in the Location Generator
    GAME TEXT is in the Game Text Generator*/

    // BADGE AND SCORE UPDATES
    public boolean awardBadge(String questType) {
        for(Badge bad : badges){
            if(bad.getType().toLowerCase().equals(questType)){
                return currentPlayer.addBadge(bad);
            }
        }
        return false;
    }

    //Getter and Setter
    public int getCurrentGameScore() {
        return currentPlayer.getPoints();
    }

    public void setCurrentGameScore(int currentGameScore) {
        currentPlayer.setPoints(currentGameScore);
    }

    public List<Badge> getBadges() {
        return badges;
    }

    private void setBadges(List<Badge> badges) {
        this.badges.clear();
        for(Badge badge: badges){
            this.badges.add(badge);
        }
    }

    private int getCurrentLevel() {
        return currentLevel;
    }

    private void setCurrentLevel(int currentLevel) {
        this.currentLevel = currentLevel;
    }
}
