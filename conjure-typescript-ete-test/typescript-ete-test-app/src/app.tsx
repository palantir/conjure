/*
 * Copyright 2015 Palantir Technologies, Inc. All rights reserved.
 */

import "es6-shim";
import "whatwg-fetch";

import * as React from "react";
import * as ReactDOM from "react-dom";

const appElement = document.getElementById("app");

if (appElement != null) {
    ReactDOM.render((
        <div>
          Nothing here
        </div>
    ), appElement);
}
