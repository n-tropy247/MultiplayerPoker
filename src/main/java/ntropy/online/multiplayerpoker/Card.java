/*
 * Copyright (C) 2019 Ryan Castelli
 * Copyright (C) 2019 Samantha Cole
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ntropy.online.multiplayerpoker;

/**
 * Class to store info about each card.
 *
 * @author NTropy
 * @author Sam Cole
 * @version 4.8.2019
 * @since 4.8.2019
 */
public final class Card {

    /**
     * Card positioning info.
     */
    private final int xPos, yPos, width, height;

    /**
     * Card states.
     */
    private boolean active = false, fill = false;

    /**
     * Default constructor.
     *
     * @param x x-position of card
     * @param y y-position of card
     * @param w width of card
     * @param h height of card
     */
    public Card(final int x, final int y, final int w, final int h) {
        xPos = x;
        yPos = y;
        width = w;
        height = h;
    }

    /**
     * Report x-position of card.
     *
     * @return x coordinate
     */
    public int getX() {
        return xPos;
    }

    /**
     * Report y-position of card.
     *
     * @return y coordinate
     */
    public int getY() {
        return yPos;
    }

    /**
     * Report width of card.
     *
     * @return width
     */
    public int getW() {
        return width;
    }

    /**
     * Report height of card.
     *
     * @return height
     */
    public int getH() {
        return height;
    }

    /**
     * Report whether card is active (highlighted).
     *
     * @return active
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Report whether card is filled in.
     *
     * @return fill
     */
    public boolean isFilled() {
        return fill;
    }

    /**
     * Change whether card is active (highlighted).
     *
     * @param b new value of active
     */
    public void setActive(final boolean b) {
        active = b;
    }

    /**
     * Change whether card is filled in.
     *
     * @param b new value of fill
     */
    public void setFill(final boolean b) {
        fill = b;
    }
}
