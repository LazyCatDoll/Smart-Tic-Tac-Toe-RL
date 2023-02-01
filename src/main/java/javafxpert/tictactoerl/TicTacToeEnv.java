
/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package javafxpert.tictactoerl;

import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.environment.Environment;
import burlap.mdp.singleagent.environment.EnvironmentOutcome;
import burlap.mdp.singleagent.environment.extensions.EnvironmentObserver;
import burlap.mdp.singleagent.environment.extensions.EnvironmentServerInterface;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.XMLFormatter;

/**
 * @author James L. Weaver (Twitter: @JavaFXpert)
 */
public class TicTacToeEnv implements Environment, EnvironmentServerInterface {
  private static int WIN_REWARD = 10;
  private static int LOSE_REWARD = -10;
  private static int MOVE_REWARD = -1;

  /**
   * String representation of cells on the game board.
   * For example: "XOIIXOXIO"
   */
  private StringBuffer gameBoard;

  /**
   * Game status, specifically, whether the game is in-progress, or if X won,
   * or if O won, or if it is cat's game (nobody won).
   */
  private String gameStatus;

  /**
   * Indicates whether the the game is in the terminal state
   */
  private boolean terminated = false;

  /**
   * Reward given for the current action
   */
  private int reward = 0;

  /**
   * Most recent state, to be returned by currentObservation() method
   */
  TicTacToeState currentObservationState;

  protected List<EnvironmentObserver> observers = new LinkedList<EnvironmentObserver>();

  /**
   * Mark that the player embedded in the environment plays
   */
  private char envPlayerMark = TicTacToeState.O_MARK;

  /**
   * Mark that the opposing player plays
   */
  private char opposingPlayerMark = TicTacToeState.X_MARK;

  public TicTacToeEnv() {
    resetEnvironment();
  }

  @Override
  public void resetEnvironment() {
    gameBoard = new StringBuffer(TicTacToeState.EMPTY_BOARD);
    if (envPlayerMark == TicTacToeState.X_MARK) {
      playRandomCell();
    }
    gameStatus = TicTacToeState.GAME_STATUS_IN_PROGRESS;

    currentObservationState = new TicTacToeState(gameBoard.toString(), gameStatus);

    terminated = false;
  }

  @Override
  public void addObservers(EnvironmentObserver... observers) {
    for(EnvironmentObserver o : observers){
      this.observers.add(o);
    }
  }

  @Override
  public void clearAllObservers() {
    this.observers.clear();
  }

  @Override
  public void removeObservers(EnvironmentObserver... observers) {
    for(EnvironmentObserver o : observers){
      this.observers.remove(o);
    }
  }

  @Override
  public List<EnvironmentObserver> observers() {
    return this.observers;
  }

  @Override
  public State currentObservation() {
    return currentObservationState;
  }

  @Override
  public EnvironmentOutcome executeAction(Action action) {
    MoveAction moveAction = (MoveAction)action;

    TicTacToeState priorState = new TicTacToeState(gameBoard.toString(), gameStatus);

    // actionId is the same as the cell number (0 - 8) of the move
    int cellNum = moveAction.getActionId();

    if (cellNum < 0 || cellNum >= TicTacToeState.NUM_CELLS ||
        (gameBoard.charAt(cellNum) != TicTacToeState.EMPTY)) {

      // Illegal move attempted so don't change
      System.out.println("Illegal move attempted to cell " + cellNum);
    }
    else {
      gameBoard.setCharAt(cellNum, opposingPlayerMark);
    }

    gameStatus = evalGameStatus();
    //if (gameStatus.equals(envPlayerMark)) {
    if (gameStatus.toCharArray()[0] == envPlayerMark) {
      reward = LOSE_REWARD;
      terminated = true;
    }
    else if (gameStatus.toCharArray()[0] == opposingPlayerMark) {

      // TODO: Consider removing this condition, as it doen't seem possible to encounter
      reward = WIN_REWARD;
      terminated = true;
    }
    else if (gameStatus.equals(TicTacToeState.GAME_STATUS_CATS_GAME)) {
      reward = MOVE_REWARD;
      terminated = true;
    }
    else {
      reward = MOVE_REWARD;
      terminated = false;

      // Uncomment to employ a strategy of playing the first empty cell with an "O"
      //playFirstEmptyCell();

      // Uncomment to employ a strategy of playing a completely random empty cell with an "O"
      //playRandomCell();

      // Uncomment to employ a strategy that randomly places "O" except when there are opportunities to block an "X" three-in-a row
      //blockOrPlayRandom();

      // Uncomment to employ a strategy that randomly places "O" except when
      // there are opportunities to play third "O" in a row, or to block an "X" three-in-a row
      //winOrblockOrPlayRandom();

      // Uncomment to employ a strategy that prefers center and random corner placement,
      // except when there are opportunities to play a third "O" in a row, or block an "X" three-in-a row
      winOrBlockOrCenterOrRandomCornerOrPlayRandom();

      gameStatus = evalGameStatus();  // Evaluate game status after opposing player has responded, and update terminated state
      if (gameStatus.toCharArray()[0] == envPlayerMark) {
        reward = LOSE_REWARD;
        terminated = true;
      }
      else if (gameStatus.toCharArray()[0] == opposingPlayerMark) {

        // TODO: Consider removing this condition, as it doen't seem possible to encounter
        reward = WIN_REWARD;
        terminated = true;
      }
      else if (gameStatus.equals(TicTacToeState.GAME_STATUS_CATS_GAME)) {
        // TODO: Consider removing this condition, as it doen't seem possible to encounter
        reward = MOVE_REWARD;
        terminated = true;
      }
    }

    TicTacToeState newState = new TicTacToeState(gameBoard.toString(), gameStatus);

    currentObservationState = newState.copy();

    EnvironmentOutcome environmentOutcome =
        new EnvironmentOutcome(priorState, action, newState, reward, terminated);

    return environmentOutcome;
  }

  @Override
  public double lastReward() {
    return reward;
  }

  @Override
  public boolean isInTerminalState() {
    return terminated;
  }

  /**
   * Indicate to the environment which mark it will play as (X or O)
   * @param envPlayerMark
   */
  public void setEnvPlayerMark(char envPlayerMark) {
    this.envPlayerMark = envPlayerMark;
    if (envPlayerMark == TicTacToeState.X_MARK) {
      opposingPlayerMark = TicTacToeState.O_MARK;
    }
    else {
      opposingPlayerMark = TicTacToeState.X_MARK;
    }
    resetEnvironment();
  }

  /**
   * Evaluate the status of the game (in-progress, or who won)
   *
   * @return Indicator of in-progress, or who won
   */
  private String evalGameStatus() {
    // Start with the assumption that all cells are occupied but nobody won
    String gameStatus = TicTacToeState.GAME_STATUS_CATS_GAME;

    // Check if this game is still in progress
    for (int idx = 0; idx < TicTacToeState.NUM_CELLS; idx++) {
      if (gameBoard.charAt(idx) ==  TicTacToeState.EMPTY) {
        gameStatus = TicTacToeState.GAME_STATUS_IN_PROGRESS;
        break;
      }
    }

    // Check if X won
    if ((gameBoard.charAt(0) == TicTacToeState.X_MARK && gameBoard.charAt(1) == TicTacToeState.X_MARK && gameBoard.charAt(2) == TicTacToeState.X_MARK) ||
        (gameBoard.charAt(3) == TicTacToeState.X_MARK && gameBoard.charAt(4) == TicTacToeState.X_MARK && gameBoard.charAt(5) == TicTacToeState.X_MARK) ||
        (gameBoard.charAt(6) == TicTacToeState.X_MARK && gameBoard.charAt(7) == TicTacToeState.X_MARK && gameBoard.charAt(8) == TicTacToeState.X_MARK) ||
        (gameBoard.charAt(0) == TicTacToeState.X_MARK && gameBoard.charAt(3) == TicTacToeState.X_MARK && gameBoard.charAt(6) == TicTacToeState.X_MARK) ||
        (gameBoard.charAt(1) == TicTacToeState.X_MARK && gameBoard.charAt(4) == TicTacToeState.X_MARK && gameBoard.charAt(7) == TicTacToeState.X_MARK) ||
        (gameBoard.charAt(2) == TicTacToeState.X_MARK && gameBoard.charAt(5) == TicTacToeState.X_MARK && gameBoard.charAt(8) == TicTacToeState.X_MARK) ||
        (gameBoard.charAt(0) == TicTacToeState.X_MARK && gameBoard.charAt(4) == TicTacToeState.X_MARK && gameBoard.charAt(8) == TicTacToeState.X_MARK) ||
        (gameBoard.charAt(2) == TicTacToeState.X_MARK && gameBoard.charAt(4) == TicTacToeState.X_MARK && gameBoard.charAt(6) == TicTacToeState.X_MARK)) {
      gameStatus = TicTacToeState.GAME_STATUS_X_WON;