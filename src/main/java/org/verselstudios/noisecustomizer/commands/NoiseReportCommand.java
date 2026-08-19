package org.verselstudios.noisecustomizer.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import org.verselstudios.noisecustomizer.NoiseCustomizer;
import org.verselstudios.noisecustomizer.utils.NoiseTracker;

public class NoiseReportCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("noisereport")
                        .requires(source -> source.hasPermission(2))

                        .then(Commands.argument("showUnknown", BoolArgumentType.bool())
                                .then(Commands.argument("showBlended", BoolArgumentType.bool())
                                        .then(Commands.argument("showNormal", BoolArgumentType.bool())
                                                .executes(context -> {

                                                    boolean showUnknown =
                                                            BoolArgumentType.getBool(context, "showUnknown");

                                                    boolean showBlended =
                                                            BoolArgumentType.getBool(context, "showBlended");

                                                    boolean showNormal =
                                                            BoolArgumentType.getBool(context, "showNormal");

                                                    runReport(
                                                            context.getSource(),
                                                            showUnknown,
                                                            showBlended,
                                                            showNormal
                                                    );

                                                    return 1;
                                                })
                                        )
                                )
                        )
        );
    }

    private static void runReport(
            CommandSourceStack source,
            boolean showUnknown,
            boolean showBlended,
            boolean showNormal
    ) {
        String report = NoiseTracker.report(
                showUnknown,
                showBlended,
                showNormal
        );

        source.sendSuccess(
                () -> Component.literal("Noise report generated. Check logs."),
                false
        );

        StringBuilder b = new StringBuilder();

        NoiseTracker.findFirstBreak(
                b,
                showUnknown,
                showBlended,
                showNormal
        );

        for (String s : b.toString().split("\n")) {
            source.sendSuccess(
                    () -> Component.literal(s),
                    false
            );
        }

        NoiseCustomizer.LOGGER.info("\n{}", report);
    }
}