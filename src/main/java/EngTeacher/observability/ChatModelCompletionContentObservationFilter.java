package EngTeacher.observability;

import io.micrometer.common.KeyValue;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationFilter;
import org.jspecify.annotations.NonNull;
import org.springframework.ai.chat.observation.ChatModelObservationContext;
import org.springframework.ai.content.Content;
import org.springframework.ai.observation.ObservabilityHelper;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;

@Component
public class ChatModelCompletionContentObservationFilter implements ObservationFilter {

    @Override
    public Observation.Context map(Observation.@NonNull Context context) {
        if (!(context instanceof ChatModelObservationContext chatContext)) {
            return context;
        }

        List<String> prompts = processPrompts(chatContext);
        List<String> completions = processCompletion(chatContext);

        chatContext.addHighCardinalityKeyValue(new KeyValue() {
            @Override
            public String getKey() {
                return "gen_ai.prompt";
            }

            @Override
            public String getValue() {
                return ObservabilityHelper.concatenateStrings(prompts);
            }
        });
        chatContext.addHighCardinalityKeyValue(new KeyValue() {
            @Override
            public String getKey() {
                return "gen_ai.completion";
            }

            @Override
            public String getValue() {
                return ObservabilityHelper.concatenateStrings(completions);
            }
        });

        return chatContext;
    }

    private List<String> processPrompts(ChatModelObservationContext context) {
        var instructions = context.getRequest().getInstructions();
        if (CollectionUtils.isEmpty(instructions)) {
            return List.of();
        }
        return instructions.stream().map(Content::getText).toList();
    }

    private List<String> processCompletion(ChatModelObservationContext context) {
        if (context.getResponse() == null
                || context.getResponse().getResults() == null
                || CollectionUtils.isEmpty(context.getResponse().getResults())) {
            return List.of();
        }
        return context.getResponse().getResults().stream()
                .filter(g -> g.getOutput() != null && StringUtils.hasText(g.getOutput().getText()))
                .map(g -> g.getOutput().getText())
                .toList();
    }
}
