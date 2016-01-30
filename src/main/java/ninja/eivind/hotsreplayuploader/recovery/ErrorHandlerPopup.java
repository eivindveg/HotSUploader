// Copyright 2015 Eivind Vegsundvåg
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package ninja.eivind.hotsreplayuploader.recovery;

import javafx.stage.PopupWindow;

public abstract class ErrorHandlerPopup extends PopupWindow implements ErrorHandler {

    private final String message;
    private final Exception exception;
    private final Runnable errorHandler;

    public ErrorHandlerPopup(final Exception exception, final Runnable errorHandler) {
        this(exception.getMessage(), exception, errorHandler);
    }

    public ErrorHandlerPopup(final String message, final Exception exception, final Runnable errorHandler) {
        this.message = message;
        this.exception = exception;
        this.errorHandler = errorHandler;
    }

    public Runnable getErrorHandler() {
        return errorHandler;
    }
}
