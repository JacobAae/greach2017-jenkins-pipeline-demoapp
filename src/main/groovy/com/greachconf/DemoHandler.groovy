package com.greachconf

import ratpack.handling.Context
import ratpack.handling.Handler


class DemoHandler implements Handler {

    @Override
    void handle(Context ctx) throws Exception {
        String name = ctx.request.queryParams.get('name') ?: 'Groovy Person'
        ctx.render("Hi ${name}, Greach is awesome!")
    }
}
