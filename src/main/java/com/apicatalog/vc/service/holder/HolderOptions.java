package com.apicatalog.vc.service.holder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

import com.apicatalog.ld.DocumentError;
import com.apicatalog.vc.service.Constants;

import io.vertx.core.json.JsonArray;
import io.vertx.ext.web.RoutingContext;

public record HolderOptions(
        Collection<String> selectivePointers
        ) {
    
    static final HolderOptions getOptions(RoutingContext ctx) throws DocumentError {

        var body = ctx.body().asJsonObject();

        var options = body.getJsonObject(Constants.OPTIONS);

        // default values
        Collection<String> selectivePointers = Collections.emptyList(); 

        // request options
        if (options != null) {
            selectivePointers = getPointers(options.getJsonArray(Constants.OPTION_SELECTIVE_POINTERS));
            
            var unknown = options.stream()
                    .filter(e -> !Constants.OPTIONS_KEYS.contains(e.getKey()))
                    .map(e -> e.getKey() + ": " + e.getValue())
                    .collect(Collectors.joining(", "));

            if (unknown != null && !unknown.isBlank()) {
                System.out.println("UNKNOWN OPTIONS [" + unknown + "]");
            }
        }

        return new HolderOptions(selectivePointers);
    }

    protected static Collection<String> getPointers(JsonArray input) {
        if (input == null) {
            return null;
        }
        
        var pointers = new ArrayList<String>(input.size());
        
        for (int i=0; i < input.size(); i++) {
            pointers.add(input.getString(i));
        }
        return pointers;
    }
    
}
