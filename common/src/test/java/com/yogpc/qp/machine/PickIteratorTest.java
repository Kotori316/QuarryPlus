package com.yogpc.qp.machine;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.*;

class PickIteratorTest {
    static class ToInt extends PickIterator<Integer> {
        private final int start;
        private final int end;
        private int cursor;

        ToInt(int start, int end) {
            this.start = start;
            this.end = end;
            this.cursor = start;
        }

        @Override
        protected Integer update() {
            int returnValue = cursor;
            if (returnValue >= end) {
                throw new NoSuchElementException();
            }
            cursor += 1;
            return returnValue;
        }

        @Override
        public boolean hasNext() {
            return cursor < end;
        }

        @Override
        public String toString() {
            return "ToInt{" +
                "start=" + start +
                ", end=" + end +
                ", cursor=" + cursor +
                '}';
        }

        @Override
        public void setLastReturned(Integer lastReturned) {
            super.setLastReturned(lastReturned);
            cursor = lastReturned + 1;
        }
    }

    @Test
    void getAll() {
        Iterable<Integer> itr = () -> new ToInt(0, 10);
        var all = StreamSupport.stream(itr.spliterator(), false).toList();
        assertEquals(10, all.size());
        assertIterableEquals(List.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9), all);
    }

    @Test
    void peek() {
        var itr = new ToInt(0, 10);
        assertTrue(itr.hasNext());
        itr.next();
        itr.next();
        itr.next();
        assertEquals(2, itr.getLastReturned());
        assertEquals(3, itr.next());
        assertEquals(3, itr.getLastReturned());
        assertEquals(3, itr.getLastReturned());
    }

    @Test
    void setCursor() {
        var itr = new ToInt(0, 10);
        itr.setLastReturned(3);
        assertTrue(itr.hasNext());
        assertEquals(3, itr.getLastReturned());
        assertEquals(4, itr.next());
    }

    @Test
    void setCursor2() {
        var itr = new ToInt(0, 10);
        itr.setLastReturned(9);
        assertFalse(itr.hasNext());
        assertThrows(NoSuchElementException.class, itr::next);
    }

    @Nested
    class SingleTest {
        @Test
        void instance() {
            var itr = assertDoesNotThrow(() -> new PickIterator.Single<>("a"));
            assertTrue(itr.hasNext());
            assertEquals("a", itr.next());
            assertFalse(itr.hasNext());
            assertEquals("a", itr.getLastReturned());
            assertThrows(NoSuchElementException.class, itr::next);
        }

        @Test
        void setLastReturned() {
            var itr = assertDoesNotThrow(() -> new PickIterator.Single<>("a"));
            assertEquals("a", itr.next());
            itr.setLastReturned(null);
            assertTrue(itr.hasNext());
        }
    }
}
