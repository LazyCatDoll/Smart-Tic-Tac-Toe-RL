
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

import burlap.mdp.auxiliary.DomainGenerator;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.SADomain;
import burlap.shell.visual.VisualExplorer;
import burlap.visualizer.StatePainter;
import burlap.visualizer.StateRenderLayer;
import burlap.visualizer.Visualizer;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

/**
 * @author James L. Weaver (Twitter: @JavaFXpert)
 */
public class TicTacToeWorld implements DomainGenerator {
  @Override
  public SADomain generateDomain() {
    SADomain domain = new SADomain();

    domain.addActionType(new MoveActionType());
    return domain;
  }

  public StateRenderLayer getStateRenderLayer(){
    StateRenderLayer rl = new StateRenderLayer();
    rl.addStatePainter(new TicTacToeWorld.WallPainter());
    rl.addStatePainter(new TicTacToeWorld.AgentPainter());


    return rl;
  }

  public Visualizer getVisualizer(){
    return new Visualizer(this.getStateRenderLayer());
  }

  public class WallPainter implements StatePainter {
    private int NUM_ROWS_COLS = 3;

    public void paint(Graphics2D g2, State s, float cWidth, float cHeight) {

      g2.setStroke(new BasicStroke(5));

      g2.setColor(Color.BLACK);

      //set up floats for the width and height of our domain
      float fWidth = NUM_ROWS_COLS;
      float fHeight = NUM_ROWS_COLS;

      //determine the width of a single cell
      //on our canvas such that the whole map can be painted
      float width = cWidth / fWidth;
      float height = cHeight / fHeight;

      for(int col = 1; col < NUM_ROWS_COLS; col++){
        float rx = col * width;
        g2.drawLine((int)rx, 0, (int)rx, (int)cHeight);
      }

      for(int row = 1; row < NUM_ROWS_COLS; row++){
        float ry = row * height;
        g2.drawLine(0, (int)ry, (int)cWidth, (int)ry);
      }
    }
  }

  public class AgentPainter implements StatePainter {

    private int NUM_ROWS = 3;
    private int NUM_COLS = 3;

    @Override
    public void paint(Graphics2D g2, State s,
                      float cWidth, float cHeight) {

      g2.setStroke(new BasicStroke(5));

      //marks will be filled in gray
      g2.setColor(Color.BLUE);

      //set up floats for the width and height of our domain
      float fWidth = NUM_COLS;
      float fHeight = NUM_ROWS;

      //determine the width of a single cell on our canvas
      //such that the whole map can be painted
      float width = cWidth / fWidth;
      float height = cHeight / fHeight;

      String gameBoard = (String)s.get(TicTacToeState.VAR_GAME_BOARD);

      //pass through each cell of our board, and it it's an X or O, it on our
      //canvas of dimension width x height
      for(int col = 0; col < NUM_ROWS; col++){
        for(int row = 0; row < NUM_COLS; row++){