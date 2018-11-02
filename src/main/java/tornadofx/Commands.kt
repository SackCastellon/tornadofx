@file:Suppress("UNCHECKED_CAST")

package tornadofx

import javafx.application.Platform
import javafx.application.Platform.isFxApplicationThread
import javafx.beans.binding.BooleanExpression
import javafx.beans.property.*
import javafx.scene.control.ButtonBase
import javafx.scene.control.ChoiceBox
import javafx.scene.control.MenuItem
import javafx.scene.control.TextField
import kotlin.annotation.AnnotationTarget.*
import kotlin.concurrent.thread

/**
 * Marks the API that is dependent on the experimental commands, including those types themselves.
 *
 * Usages of such API will be reported as warnings unless an explicit opt-in with
 * the [UseExperimental] annotation, e.g. `@UseExperimental(ExperimentalCommands::class)`,
 * or with the `-Xuse-experimental=kotlin.ExperimentalCommands` compiler option is given.
 *
 * It's recommended to propagate the experimental status to the API that depends on commands by annotating it with this annotation.
 */
@Experimental
@Target(CLASS, ANNOTATION_CLASS, PROPERTY, FIELD, LOCAL_VARIABLE, VALUE_PARAMETER, CONSTRUCTOR, FUNCTION, PROPERTY_GETTER, PROPERTY_SETTER, TYPEALIAS)
@Retention(AnnotationRetention.BINARY)
annotation class ExperimentalCommands


internal const val COMMAND_VALUE_PROPERTY = "tornadofx.commandValue"
internal const val COMMAND_PARAM_PROPERTY = "tornadofx.commandParam"


@ExperimentalCommands
open class Command<in T>(
    val action: (T?) -> Unit,
    val enabled: BooleanExpression = SimpleBooleanProperty(true),
    val async: Boolean = false,
    val ui: Boolean = false
) {
    val running: ReadOnlyBooleanProperty = SimpleBooleanProperty(false)
    val isRunning: Boolean get() = running.value
    val isEnabled: Boolean get() = enabled.value

    internal val disabledProperty = enabled.not().or(running)

    fun execute(): Unit = execute(null as T?)

    fun execute(param: T?) {
        if (isRunning || disabledProperty.value) return
        if (async) thread(true) { doRun(param) } else doRun(param)
    }

    private fun doRun(param: T?) {
        if (ui && !isFxApplicationThread()) {
            if (async) {
                Platform.runLater { setRunningAndRun(param) }
            } else {
                FX.runAndWait { setRunningAndRun(param) }
            }
        } else {
            setRunningAndRun(param)
        }
    }

    private fun setRunningAndRun(param: T?) {
        (running as BooleanProperty).value = true
        try {
            action(param)
        } finally {
            running.value = false
        }
    }
}


/**
 * Create a command with a non null parameter where the is a function reference.
 */
@ExperimentalCommands
fun <T> command(
    action: (T) -> Unit,
    enabled: BooleanExpression = SimpleBooleanProperty(true),
    async: Boolean = false,
    ui: Boolean = false
): Command<T> = Command({ action(it!!) }, enabled, async, ui)

/**
 * Create a command with a nullable parameter where the is either a lambda or a function reference.
 *
 * The noarg parameter is useless, but a trick to help Kotlin differentiate between the no null parameter version of this function.
 */
@ExperimentalCommands
fun <T> command(
    action: (T?) -> Unit,
    enabled: BooleanExpression = SimpleBooleanProperty(true),
    async: Boolean = false,
    ui: Boolean = false,
    @Suppress("UNUSED_PARAMETER") nullable: Boolean = true
): Command<T?> = Command({ action(it) }, enabled, async, ui)

/**
 * Create a command with a nullable parameter where the is a lambda.
 *
 * The noarg parameter is useless, but a trick to help Kotlin differentiate between the no null parameter version of this function.
 */
@ExperimentalCommands
fun <T> command(
    enabled: BooleanExpression = SimpleBooleanProperty(true),
    async: Boolean = false,
    ui: Boolean = false,
    @Suppress("UNUSED_PARAMETER") nullable: Boolean = true,
    action: (T?) -> Unit
): Command<T?> = Command({ action(it) }, enabled, async, ui)

/**
 * Create a parameterless command where the action is a function reference.
 */
@ExperimentalCommands
fun command(
    action: () -> Unit,
    enabled: BooleanExpression = SimpleBooleanProperty(true),
    async: Boolean = false,
    ui: Boolean = false
): Command<Any> = Command({ action() }, enabled, async, ui)

/**
 * Create a parameterless command where the action is a lambda.
 * This overload allows the command to be defined as the last parameter
 */
@ExperimentalCommands
fun command(
    enabled: BooleanExpression = SimpleBooleanProperty(true),
    async: Boolean = false,
    ui: Boolean = false,
    action: () -> Unit
): Command<Any> = Command({ action() }, enabled, async, ui)

@ExperimentalCommands
var ButtonBase.commandProperty: ObjectProperty<Command<*>>
    get() = properties.getOrPut(COMMAND_VALUE_PROPERTY) {
        SimpleObjectProperty<Command<*>>().apply {
            onChange {
                if (it == null) disableProperty().unbind()
                else disableProperty().cleanBind(it.disabledProperty)
            }
            this@commandProperty.action { (value as? Command<Any?>)?.execute(commandParameter) }
        }
    } as ObjectProperty<Command<*>>
    set(value) {
        properties[COMMAND_VALUE_PROPERTY] = value
    }

@ExperimentalCommands
var ButtonBase.command: Command<*>
    get() = commandProperty.value
    set(value) {
        if (value is CommandWithParameter) {
            commandProperty.value = value.command
            commandParameter = value.parameter
        } else {
            commandProperty.value = value as Command<Any?>
        }
    }

@ExperimentalCommands
var ButtonBase.commandParameterProperty: Property<Any?>
    get() = properties.getOrPut(COMMAND_PARAM_PROPERTY) {
        SimpleObjectProperty<Any?>()
    } as Property<Any?>
    set(value) {
        properties[COMMAND_PARAM_PROPERTY] = value
    }

@ExperimentalCommands
var ButtonBase.commandParameter: Any?
    get() = commandParameterProperty.value
    set(value) {
        if (value is Property<*>) {
            commandParameterProperty = value as Property<Any?>
        } else {
            commandParameterProperty.value = value
        }
    }

@ExperimentalCommands
var MenuItem.commandProperty: Property<Command<*>>
    get() = properties.getOrPut(COMMAND_VALUE_PROPERTY) {
        SimpleObjectProperty<Command<*>>().apply {
            onChange {
                if (it == null) disableProperty().unbind()
                else disableProperty().cleanBind(it.disabledProperty)
            }
            this@commandProperty.action { (value as? Command<Any?>)?.execute(commandParameter) }
        }
    } as Property<Command<*>>
    set(value) {
        properties[COMMAND_VALUE_PROPERTY] = value
    }

@ExperimentalCommands
var MenuItem.command: Command<*>
    get() = commandProperty.value
    set(value) {
        if (value is CommandWithParameter) {
            commandProperty.value = value.command
            commandParameter = value.parameter
        } else {
            commandProperty.value = value as Command<Any?>
        }
    }

@ExperimentalCommands
var MenuItem.commandParameterProperty: Property<Any?>
    get() = properties.getOrPut(COMMAND_PARAM_PROPERTY) {
        SimpleObjectProperty<Any?>()
    } as Property<Any?>
    set(value) {
        properties[COMMAND_PARAM_PROPERTY] = value
    }

@ExperimentalCommands
var MenuItem.commandParameter: Any?
    get() = commandParameterProperty.value
    set(value) {
        if (value is Property<*>) {
            commandParameterProperty = value as Property<Any?>
        } else {
            commandParameterProperty.value = value
        }
    }

@ExperimentalCommands
var TextField.commandProperty: Property<Command<*>>
    get() = properties.getOrPut(COMMAND_VALUE_PROPERTY) {
        SimpleObjectProperty<Command<*>>().apply {
            onChange {
                if (it == null) disableProperty().unbind()
                else disableProperty().cleanBind(it.disabledProperty)
            }
            this@commandProperty.action { (value as? Command<Any?>)?.execute(commandParameter) }
        }
    } as Property<Command<*>>
    set(value) {
        properties[COMMAND_VALUE_PROPERTY] = value
    }

@ExperimentalCommands
var TextField.command: Command<*>
    get() = commandProperty.value
    set(value) {
        if (value is CommandWithParameter) {
            commandProperty.value = value.command
            commandParameter = value.parameter
        } else {
            commandProperty.value = value as Command<Any?>
        }
    }

@ExperimentalCommands
var TextField.commandParameterProperty: Property<Any?>
    get() = properties.getOrPut(COMMAND_PARAM_PROPERTY) {
        SimpleObjectProperty<Any?>()
    } as Property<Any?>
    set(value) {
        properties[COMMAND_PARAM_PROPERTY] = value
    }

@ExperimentalCommands
var TextField.commandParameter: Any?
    get() = commandParameterProperty.value
    set(value) {
        if (value is Property<*>) {
            commandParameterProperty = value as Property<Any?>
        } else {
            commandParameterProperty.value = value
        }
    }

@ExperimentalCommands
var ChoiceBox<*>.commandProperty: Property<Command<*>>
    get() = properties.getOrPut(COMMAND_VALUE_PROPERTY) {
        SimpleObjectProperty<Command<*>>().apply {
            onChange {
                if (it == null) disableProperty().unbind()
                else disableProperty().cleanBind(it.disabledProperty)
            }
            this@commandProperty.action { (value as? Command<Any?>)?.execute(commandParameter) }
        }
    } as Property<Command<*>>
    set(value) {
        properties[COMMAND_VALUE_PROPERTY] = value
    }

@ExperimentalCommands
var ChoiceBox<*>.command: Command<*>
    get() = commandProperty.value
    set(value) {
        if (value is CommandWithParameter) {
            commandProperty.value = value.command
            commandParameter = value.parameter
        } else {
            commandProperty.value = value as Command<Any?>
        }
    }

@ExperimentalCommands
var ChoiceBox<*>.commandParameterProperty: Property<Any?>
    get() = properties.getOrPut(COMMAND_PARAM_PROPERTY) {
        SimpleObjectProperty<Any?>()
    } as Property<Any?>
    set(value) {
        properties[COMMAND_PARAM_PROPERTY] = value
    }

@ExperimentalCommands
var ChoiceBox<*>.commandParameter: Any?
    get() = commandParameterProperty.value
    set(value) {
        if (value is Property<*>) {
            commandParameterProperty = value as Property<Any?>
        } else {
            commandParameterProperty.value = value
        }
    }

@ExperimentalCommands
class CommandWithParameter(val command: Command<*>, val parameter: Any?) : Command<Any?>({})

@ExperimentalCommands
operator fun Command<*>.invoke(parameter: Any?): CommandWithParameter = CommandWithParameter(this, parameter)

@ExperimentalCommands
infix fun Command<*>.with(parameter: Any?): CommandWithParameter = invoke(parameter)
