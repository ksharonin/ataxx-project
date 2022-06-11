/* Skeleton code copyright (C) 2008, 2022 Paul N. Hilfinger and the
 * Regents of the University of California.  Do not distribute this or any
 * derivative work without permission. */

package ataxx;

import ucb.gui2.Pad;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.BasicStroke;

import java.awt.event.MouseEvent;

import java.util.concurrent.ArrayBlockingQueue;

import static ataxx.PieceColor.*;
import static ataxx.Utils.*;

/** Widget for displaying an Ataxx board.
 *  @author Katrina Sharonin
 */
class BoardWidget extends Pad  {

    /** Length of side of one square, in pixels. */
    static final int SQDIM = 50;
    /** Number of squares on a side. */
    static final int SIDE = Board.SIDE;
    /** Radius of circle representing a piece. */
    static final int PIECE_RADIUS = 15;
    /** Dimension of a block. */
    static final int BLOCK_WIDTH = 40;

    /** Color of red pieces. */
    private static final Color RED_COLOR = Color.RED;
    /** Color of blue pieces. */
    private static final Color BLUE_COLOR = Color.BLUE;
    /** Color of painted lines. */
    private static final Color LINE_COLOR = Color.BLACK;
    /** Color of blank squares. */
    private static final Color BLANK_COLOR = Color.WHITE;
    /** Color of selected squared. */
    private static final Color SELECTED_COLOR = new Color(150, 150, 150);
    /** Color of blocks. */
    private static final Color BLOCK_COLOR = Color.BLACK;

    /** Stroke for lines. */
    private static final BasicStroke LINE_STROKE = new BasicStroke(1.0f);
    /** Stroke for blocks. */
    private static final BasicStroke BLOCK_STROKE = new BasicStroke(5.0f);

    /** A new widget sending commands resulting from mouse clicks
     *  to COMMANDQUEUE. */
    BoardWidget(ArrayBlockingQueue<String> commandQueue) {
        _commandQueue = commandQueue;
        setMouseHandler("click", this::handleClick);
        _dim = SQDIM * SIDE;
        _blockMode = false;
        setPreferredSize(_dim, _dim);
        setMinimumSize(_dim, _dim);
    }

    /** Indicate that SQ (of the form CR) is selected, or that none is
     *  selected if SQ is null. */
    void selectSquare(String sq) {
        if (sq == null) {
            _selectedCol = _selectedRow = 0;
        } else {
            _selectedCol = sq.charAt(0);
            _selectedRow = sq.charAt(1);
        }
        repaint();
    }

    @Override
    public synchronized void paintComponent(Graphics2D g) {
        g.setColor(BLANK_COLOR);
        g.fillRect(0, 0, _dim, _dim);

        g.setColor(LINE_COLOR);


        for (int hor = 0; hor < SQDIM * SIDE; hor +=  SQDIM) {
            for (int ver = 0; ver < SQDIM * SIDE; ver += SQDIM) {

                char rowy = (char) ('7' - (hor / SQDIM));
                char coly = (char) ('a' + (ver / SQDIM));

                g.setColor(LINE_COLOR);
                g.drawRect(ver, hor, SQDIM, SQDIM);


                if (_model.get(coly, rowy) == RED) {
                    g.setColor(RED_COLOR);
                    g.fillOval((ver + (SQDIM / 4)), (hor + (SQDIM / 5)),
                            PIECE_RADIUS*2, PIECE_RADIUS*2);
                }


                else if (_model.get(coly, rowy) == BLUE) {
                    g.setColor(BLUE_COLOR);
                    g.fillOval((ver + (SQDIM / 4)), (hor + (SQDIM / 5)),
                            PIECE_RADIUS * 2, PIECE_RADIUS*2);

                }

                else if(_model.get(coly, rowy) == BLOCKED) {
                    drawBlock(g,coly, rowy, ver, hor);
                }

            }
        }
    }

    /** Draw a block centered at (CX, CY) on G. */
    void drawBlock(Graphics2D g, char rowpreserved, char colpreserved, int cx, int cy) {
        /**
        char rowy2 = (char) ('7' - (cy/SQDIM));
        char coly2 = (char) ('a' + (cx/SQDIM));
        g.fillRect(cx, cy, SQDIM, SQDIM);

        int linearized = _model.index(coly2, rowy2);
        int rowDif = (linearized / 11);
        int colDif = (linearized % 11);

        // opposite / reflected block
        char rowy = (char) ('7' + 2 - rowDif);
        char coly = (char) ('g' + 2 - colDif);

        int cxModified = ('7' - rowy) * SQDIM;
        int cyModified = (coly -'a') * SQDIM;

        g.fillRect(cxModified, cyModified, SQDIM, SQDIM); */

        // 6 - operation
        // 6 - cx var

        g.setColor(BLOCK_COLOR);

        if (colpreserved == 'd' && rowpreserved == '4' ){
            // set just center
            g.fillRect(cx, cy, SQDIM-1, SQDIM-1);

        } else if (colpreserved == 'd' && rowpreserved != '4') {
            // reflect about the center line
            g.fillRect(cx, cy, SQDIM-1, SQDIM-1);
            g.fillRect(cx, 6-cy, SQDIM-1, SQDIM-1);

        } else if (colpreserved != 'd' && rowpreserved == '4') {
            g.fillRect(cx, cy, SQDIM-1, SQDIM-1);
            g.fillRect(6-cx, cy, SQDIM-1, SQDIM-1);

        } else { // reflect about by four ways
            //self
            g.fillRect(cx, cy, SQDIM-1, SQDIM-1);
            g.fillRect(cx, 6-cy, SQDIM-1, SQDIM-1);
            g.fillRect(6-cx, cy, SQDIM-1, SQDIM-1);
            g.fillRect(6-cx, 6-cy, SQDIM-1, SQDIM-1);

        }



    }

    /** Clear selected block, if any, and turn off block mode. */
    void reset() {
        _selectedRow = _selectedCol = 0;
        setBlockMode(false);
    }

    /** Set block mode on iff ON. */
    void setBlockMode(boolean on) {
        _blockMode = on;
    }

    /** Issue move command indicated by mouse-click event WHERE. */
    private void handleClick(String unused, MouseEvent where) {
        int x = where.getX(), y = where.getY();
        char mouseCol, mouseRow;
        if (where.getButton() == MouseEvent.BUTTON1) {
            mouseCol = (char) (x / SQDIM + 'a');
            mouseRow = (char) ((SQDIM * SIDE - y) / SQDIM + '1');
            if (mouseCol >= 'a' && mouseCol <= 'g'
                && mouseRow >= '1' && mouseRow <= '7') {
                if (_blockMode) {
                    _commandQueue.offer("block " + mouseCol + mouseRow);
                } else {
                    if (_selectedCol != 0) {
                        _commandQueue.offer("" + _selectedCol + _selectedRow + "-" + mouseCol + mouseRow );
                        _selectedCol = _selectedRow = 0;
                    } else {
                        _selectedCol = mouseCol;
                        _selectedRow = mouseRow;
                    }
                }
            }
        }
        repaint();
    }

    public synchronized void update(Board board) {
        _model = new Board(board);
        repaint();
    }

    /** Dimension of current drawing surface in pixels. */
    private int _dim;

    /** Model being displayed. */
    private static Board _model;

    /** Coordinates of currently selected square, or '\0' if no selection. */
    private char _selectedCol, _selectedRow;

    /** True iff in block mode. */
    private boolean _blockMode;

    /** Destination for commands derived from mouse clicks. */
    private ArrayBlockingQueue<String> _commandQueue;
}
