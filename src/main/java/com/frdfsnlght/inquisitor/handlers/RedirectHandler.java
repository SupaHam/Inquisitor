/*
 * Copyright 2012 frdfsnlght <frdfsnlght@gmail.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.frdfsnlght.inquisitor.handlers;

import java.io.IOException;

import com.frdfsnlght.inquisitor.WebHandler;
import com.frdfsnlght.inquisitor.WebRequest;
import com.frdfsnlght.inquisitor.WebResponse;

/**
 * 
 * @author frdfsnlght <frdfsnlght@gmail.com>
 */

public class RedirectHandler extends WebHandler {

	private String url;
	private int status;

	public RedirectHandler(String url, int status) {
		this.url = url;
		this.status = status;
	}

	@Override
	public void handleRequest(WebRequest req, WebResponse res)
			throws IOException {
		res.redirect(url, status);
	}

}
