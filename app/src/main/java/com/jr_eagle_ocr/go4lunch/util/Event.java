package com.jr_eagle_ocr.go4lunch.util;

import org.jetbrains.annotations.Nullable;

public class Event<T> {
    private final T content;
    private boolean hasBeenHandled;

    public Event(T content) {
        this.content = content;
    }

    public final boolean getHasBeenHandled() {
        return hasBeenHandled;
    }

    @Nullable
    public final T getContentIfNotHandled() {
        if (hasBeenHandled) {
            return null;
        } else {
            hasBeenHandled = true;
            return content;
        }
    }

    public final T peekContent() {
        return this.content;
    }
}