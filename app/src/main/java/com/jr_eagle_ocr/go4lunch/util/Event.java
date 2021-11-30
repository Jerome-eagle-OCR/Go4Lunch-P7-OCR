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
}