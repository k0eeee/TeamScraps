package entity_test;

import entity.CollisionHandler;
import entity.Food;
import entity.Segment;
import entity.Snake;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CollisionHandlerTest {

    @Test
    void check_hitsWall_outOfBounds() {
        //wall hit when head out of bound
        Snake snake = new Snake();
        CollisionHandler handler = new CollisionHandler();

        snake.getHead().set_Position(-1,3);

        CollisionHandler.Result result = handler.check(
                snake,
                null,
                null,
                false,
                10,
                10
        );

        assertEquals(CollisionHandler.Result.HIT_WALL, result);
    }

    @Test
    void check_hitsSelf_headEatsBody() {
        //die when head hit body
        Snake snake = new Snake();
        CollisionHandler handler = new CollisionHandler();

        Segment body = snake.getBody().get(0);
        snake.getHead().set_Position(body.getX(), body.getY());

        CollisionHandler.Result result = handler.check(
                snake,
                null,
                null,
                false,
                10,
                10
        );

        assertEquals(CollisionHandler.Result.HIT_SELF, result);
    }

    @Test
    void check_ateFood_whenHeadOnFood() {
        Snake snake = new Snake();
        CollisionHandler handler = new CollisionHandler();
        Food food = new Food();

        // Place head and food at same position
        snake.getHead().set_Position(4,4);
        food.setPosition(4,4);

        CollisionHandler.Result result = handler.check(
                snake,
                food,
                null,
                false,
                10,
                10
        );

        assertEquals(CollisionHandler.Result.ATE_FOOD, result);

    }
}
