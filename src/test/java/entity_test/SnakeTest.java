package entity_test;

import entity.Segment;
import entity.Snake;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SnakeTest {

    @Test
    void initializeSnakeTest() {
        //length of snake at start is 3
        Snake snake = new Snake();

        Segment head = snake.getHead();
        List<Segment> body = snake.getBody();

        assertEquals(3, body.size()+1);


        //check initial positions
        assertEquals(1, head.getX());
        assertEquals(3, head.getY());

        assertEquals(1, body.get(0).getX());
        assertEquals(2, body.get(0).getY());

        assertEquals(1, body.get(1).getX());
        assertEquals(1, body.get(1).getY());


        //check occupies() return true
        assertTrue(snake.occupies(1,3));
        assertTrue(snake.occupies(1,2));
        assertTrue(snake.occupies(1,1));
        assertFalse(snake.occupies(0,0));
    }

    @Test
    void setDirectionTest() {
        //cannot go opposite

        Snake snake = new Snake();

        snake.setDirection(Snake.Direction.LEFT);
        snake.move();
        Segment head = snake.getHead();

        assertEquals(2, head.getX());
        assertEquals(3, head.getY());

        snake.setDirection(Snake.Direction.UP);
        snake.move();
        head = snake.getHead();

        assertEquals(2, head.getX());
        assertEquals(2, head.getY());
    }

    @Test
    void moveTest() {
        //check all segment move in correct order
        Snake snake = new Snake();

        snake.move();
        Segment head = snake.getHead();
        List<Segment> body = snake.getBody();

        assertEquals(2, head.getX());
        assertEquals(3, head.getY());

        assertEquals(1, body.get(0).getX());
        assertEquals(3, body.get(0).getY());

        assertEquals(1, body.get(1).getX());
        assertEquals(2, body.get(1).getY());
    }

    @Test
    void growTest() {
        //segment will increase at tail position
        Snake snake = new Snake();
        List<Segment> bodyBefore = snake.getBody();
        int lengthBefore = bodyBefore.size() + 1;

        Segment tailBefore = bodyBefore.get(bodyBefore.size()-1);
        int tailX = tailBefore.getX();
        int tailY = tailBefore.getY();

        snake.grow();

        List<Segment> bodyAfter = snake.getBody();
        int lengthAfter = bodyAfter.size() + 1;

        assertEquals(lengthBefore+1, lengthAfter);

        Segment tailAfter = bodyAfter.get(bodyAfter.size()-1);
        assertEquals(tailX, tailAfter.getX());
        assertEquals(tailY, tailAfter.getY());
    }

}
