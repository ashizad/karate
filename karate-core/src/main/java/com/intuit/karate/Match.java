/*
 * The MIT License
 *
 * Copyright 2017 Intuit Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.intuit.karate;

import com.intuit.karate.exception.KarateException;
import com.intuit.karate.http.DummyHttpClient;
import java.io.File;
import java.util.List;
import java.util.Map;
import net.minidev.json.JSONObject;

/**
 *
 * @author pthomas3
 */
public class Match {
    
    private final ScriptContext context;
    private ScriptValue prevValue = ScriptValue.NULL;
    
    public static Match init() {
        return new Match();
    }
    
    public static Match init(String exp) {
        Match m = new Match();
        ScriptValue sv = Script.eval(exp, m.context);
        return m.putAll(sv.evalAsMap(m.context));
    }
    
    public static Match json(String exp) {
        return parse(exp);
    }

    public static Match xml(String exp) {
        return parse(exp);
    }    
    
    private static Match parse(String exp) {
        Match m = new Match();
        m.prevValue = Script.eval(exp, m.context);
        return m;
    }
    
    private Match() {
        ScriptEnv env = ScriptEnv.init(null, new File("."));
        CallContext callContext = new CallContext(null, 0, null, -1, false, false, DummyHttpClient.class.getName());
        context = new ScriptContext(env, callContext);
    }
    
    private void handleFailure(AssertionResult ar) {
        if (!ar.pass) {
            context.logger.error("{}", ar);
            throw new KarateException(ar.message);
        }
    }    
    
    public Match defText(String name, String exp) {
        prevValue = Script.assignText(name, exp, context, false);
        return this;
    }
    
    public Match putAll(Map<String, Object> map) {
        if (map != null) {
            map.forEach((k, v) -> context.vars.put(k, v));
        }
        return this;
    }
    
    public Match eval(String exp) {
        prevValue = Script.eval(exp, context);
        return this;
    }   
    
    public Match def(String name, String exp) {
        prevValue = Script.assign(name, exp, context, false);
        return this;
    }
    
    public Match def(String name, Object o) {
        prevValue = context.vars.put(name, o);
        return this;
    }
    
    public Map<String, Object> asMap(String exp) {
        eval(exp);
        return prevValue.getAsMap();
    }
    
    public Map<String, Object> asMap() {
        return prevValue.getAsMap();
    }
    
    public String asJson() {
        return JsonUtils.toJson(prevValue.getAsMap());
    }    
    
    public Map<String, Object> allAsMap() {
        return context.vars.toPrimitiveMap();
    }
    
    public String allAsJson() {
        return JsonUtils.toJson(context.vars.toPrimitiveMap());
    }    
    
    public List<Object> asList(String exp) {
        eval(exp);
        return prevValue.getAsList();
    }    
    
    public List<Object> asList() {
        return prevValue.getAsList();
    }
    
    public Match equalsText(String exp) {
        String quoted = "\"" + JSONObject.escape(exp) + "\"";
        return equals(quoted);
    }
    
    public Match equals(String exp) {
        AssertionResult result = Script.matchScriptValue(MatchType.EQUALS, prevValue, "$", exp, context);
        handleFailure(result);
        return this;
    }
    
    // ideally 'equals' but conflicts with Java
    public Match equalsObject(Object o) {
        Script.matchNestedObject('.', "$", MatchType.EQUALS, 
                prevValue.getValue(), null, null, o, context);
        return this;
    }
    
    public static Match equals(Object o, String exp) {
        Match m = Match.init();
        m.prevValue = new ScriptValue(o);
        return m.equals(exp);
    }
    
}
