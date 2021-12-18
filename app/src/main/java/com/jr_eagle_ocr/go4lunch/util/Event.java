package com.jr_eagle_ocr.go4lunch.util;

import org.jetbrains.annotations.Nullable;

/**
 * Used as a wrapper for data that is exposed via a LiveData that represents an event.
 *
 * @param <T> the type of the "content" object
 * @author based on Jose Alcérreca's Event.kt, found on medium.com
 */
public class Event<T> {
    private final T content;
    private boolean hasBeenHandled;

    public Event(T content) {
        this.content = content;
    }

    /**
     * Returns if content has already been handled
     */
    public final boolean getHasBeenHandled() {
        return hasBeenHandled;
    }

    /**
     * Returns the content and prevents its use again.
     */
    @Nullable
    public final T getContentIfNotHandled() {
        if (hasBeenHandled) {
            return null;
        } else {
            hasBeenHandled = true;
            return content;
        }
    }

    /**
     * Returns the content, even if it's already been handled.
     */
    public final T peekContent() {
        return this.content;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Event<?> event = (Event<?>) o;

        if (hasBeenHandled != event.hasBeenHandled) return false;
        return content != null ? content.equals(event.content) : event.content == null;
    }

    @Override
    public int hashCode() {
        int result = content != null ? content.hashCode() : 0;
        result = 31 * result + (hasBeenHandled ? 1 : 0);
        return result;
    }
}