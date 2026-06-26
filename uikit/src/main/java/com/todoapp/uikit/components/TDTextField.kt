@file:Suppress("TooManyFunctions")

package com.todoapp.uikit.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.todoapp.uikit.R
import com.todoapp.uikit.previews.TDPreviewForm
import com.todoapp.uikit.theme.TDTheme
import com.todoapp.uikit.theme.textFieldColors

@Composable
fun TDTextField(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    label: String? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    supportingText: String? = null,
    enabled: Boolean = true,
    singleLine: Boolean = true,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    passwordVisible: Boolean? = null,
    onTogglePasswordVisible: (() -> Unit)? = null,
    onFocusChange: ((Boolean) -> Unit)? = null,
) {
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    var isFocused by remember { mutableStateOf(false) }

    LaunchedEffect(isFocused) {
        if (isFocused) {
            bringIntoViewRequester.bringIntoView()
        }
    }

    val effectiveTrailingIcon: (@Composable () -> Unit)? =
        trailingIcon
            ?: if (passwordVisible != null && onTogglePasswordVisible != null) {
                {
                    IconButton(onClick = onTogglePasswordVisible) {
                        Icon(
                            imageVector =
                            ImageVector.vectorResource(
                                if (passwordVisible) {
                                    R.drawable.ic_visibility
                                } else {
                                    R.drawable.ic_visibility_off
                                },
                            ),
                            contentDescription = stringResource(R.string.cd_toggle_password_visibility),
                            tint =
                            if (isError) {
                                TDTheme.colors.red
                            } else if (isFocused) {
                                TDTheme.colors.pendingGray
                            } else {
                                TDTheme.colors.gray
                            },
                        )
                    }
                }
            } else {
                null
            }

    val trailingTransformation =
        if ((passwordVisible != null) && (onTogglePasswordVisible != null) && !passwordVisible) {
            PasswordVisualTransformation()
        } else {
            visualTransformation
        }

    OutlinedTextField(
        modifier =
        modifier
            .fillMaxWidth()
            .bringIntoViewRequester(bringIntoViewRequester)
            .onFocusChanged { focusState ->
                isFocused = focusState.isFocused
                onFocusChange?.invoke(focusState.isFocused)
            },
        value = value,
        onValueChange = onValueChange,
        label = {
            if (label != null) {
                TDText(text = label, style = TDTheme.typography.subheading1)
            }
        },
        singleLine = singleLine,
        enabled = enabled,
        isError = isError,
        leadingIcon = leadingIcon,
        trailingIcon = effectiveTrailingIcon,
        visualTransformation = trailingTransformation,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        colors = textFieldColors(),
        shape = RoundedCornerShape(12.dp),
        supportingText = {
            if (!supportingText.isNullOrEmpty()) {
                TDText(
                    text = supportingText,
                    style = TDTheme.typography.subheading2,
                    color = if (isError) TDTheme.colors.red else TDTheme.colors.gray,
                )
            }
        },
    )
}

@Composable
fun TDLabeledTextField(
    title: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    enabled: Boolean = true,
    isError: Boolean = false,
    singleLine: Boolean = false,
    minLines: Int = 1,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    style: TextStyle = TDTheme.typography.heading6,
    color: Color = TDTheme.colors.onSurface,
) {
    Column(modifier = modifier) {
        TDText(
            text = title,
            style = style,
            color = color,
        )

        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            isError = isError,
            singleLine = singleLine,
            minLines = minLines,
            colors = textFieldColors(),
            visualTransformation = visualTransformation,
            placeholder = {
                if (placeholder != null) {
                    TDText(
                        text = placeholder,
                        color = TDTheme.colors.gray.copy(alpha = 0.6f),
                        style = TDTheme.typography.regularTextStyle,
                    )
                }
            },
            shape = RoundedCornerShape(12.dp),
            textStyle = TDTheme.typography.regularTextStyle.copy(color = TDTheme.colors.onSurface),
        )
    }
}

@Composable
fun TDCompactOutlinedTextField(
    modifier: Modifier = Modifier,
    value: String,
    label: String? = null,
    onValueChange: (String) -> Unit,
    placeholder: String? = null,
    enabled: Boolean = true,
    isError: Boolean = false,
    singleLine: Boolean = true,
    style: TextStyle = TDTheme.typography.heading6,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    color: Color = TDTheme.colors.onSurface,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    supportingText: String? = null,
    roundedCornerShape: RoundedCornerShape = RoundedCornerShape(12.dp),
    height: Dp = 48.dp,
) {
    var isFocused by remember { mutableStateOf(false) }

    var textFieldValueState by remember {
        mutableStateOf(TextFieldValue(text = value, selection = TextRange(value.length)))
    }

    LaunchedEffect(value) {
        if (value != textFieldValueState.text) {
            textFieldValueState =
                textFieldValueState.copy(
                    text = value,
                    selection = TextRange(value.length),
                )
        }
    }

    Column(modifier = modifier.padding(vertical = 6.dp)) {
        if (!label.isNullOrEmpty()) {
            TDText(
                text = label,
                style = style,
                color = color,
            )
            Spacer(Modifier.height(8.dp))
        }
        val borderColor =
            when {
                isError -> TDTheme.colors.red
                !enabled -> TDTheme.colors.lightGray.copy(alpha = 0.38f)
                isFocused -> TDTheme.colors.pendingGray
                else -> TDTheme.colors.lightGray
            }

        val focusRequester = remember { FocusRequester() }

        Column {
            Box(
                modifier =
                Modifier
                    .height(height = height)
                    .heightIn(min = 40.dp)
                    .border(1.5.dp, borderColor, roundedCornerShape)
                    .background(
                        color = if (enabled) Color.Transparent else TDTheme.colors.background.copy(alpha = 0.5f),
                        shape = roundedCornerShape,
                    ),
                contentAlignment = Alignment.CenterStart,
            ) {
                BasicTextField(
                    value = textFieldValueState,
                    onValueChange = {
                        textFieldValueState = it
                        if (value != it.text) {
                            onValueChange(it.text)
                        }
                    },
                    enabled = enabled,
                    singleLine = singleLine,
                    visualTransformation = visualTransformation,
                    cursorBrush = SolidColor(if (isError) TDTheme.colors.red else TDTheme.colors.pendingGray),
                    textStyle =
                    TDTheme.typography.regularTextStyle.copy(
                        color =
                        if (enabled) {
                            TDTheme.colors.onSurface
                        } else {
                            TDTheme.colors.onSurface.copy(alpha = 0.38f)
                        },
                    ),
                    modifier =
                    Modifier
                        .fillMaxSize()
                        .onFocusChanged { isFocused = it.isFocused }
                        .focusRequester(focusRequester),
                    decorationBox = { innerTextField ->
                        Row(
                            modifier =
                            Modifier
                                .fillMaxSize()
                                .padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            leadingIcon?.invoke()

                            Box(
                                modifier =
                                Modifier
                                    .padding(vertical = 8.dp)
                                    .weight(1f),
                                contentAlignment = Alignment.CenterStart,
                            ) {
                                if (value.isEmpty() && !placeholder.isNullOrEmpty()) {
                                    TDText(
                                        text = placeholder,
                                        style = TDTheme.typography.regularTextStyle,
                                        color =
                                        if (enabled) {
                                            TDTheme.colors.gray.copy(alpha = 0.6f)
                                        } else {
                                            TDTheme.colors.gray.copy(alpha = 0.3f)
                                        },
                                    )
                                }
                                innerTextField()
                            }

                            trailingIcon?.invoke()
                        }
                    },
                )
            }
            if (!supportingText.isNullOrEmpty()) {
                Spacer(Modifier.height(4.dp))
                TDText(
                    text = supportingText,
                    style = TDTheme.typography.subheading2,
                    color = if (isError) TDTheme.colors.red else TDTheme.colors.gray,
                )
            }
        }
    }
}

@TDPreviewForm
@Composable
private fun TDTextFieldCompositePreview() {
    TDTheme {
        Column(
            modifier =
            Modifier
                .background(TDTheme.colors.background)
                .padding(16.dp),
        ) {
            TDTextField(value = "John Doe", onValueChange = {}, label = "First Name")
            Spacer(modifier = Modifier.height(12.dp))
            TDTextField(value = "johndoe@hotmail.com", onValueChange = {}, label = "Email")
            Spacer(modifier = Modifier.height(12.dp))
            TDTextField(
                value = "secret123",
                supportingText = "Password is too weak",
                onValueChange = {},
                label = "Password",
                isError = true,
                passwordVisible = false,
                onTogglePasswordVisible = {},
            )
            Spacer(modifier = Modifier.height(24.dp))
            TDCompactOutlinedTextField(
                value = "",
                onValueChange = {},
                placeholder = "Task Title",
                label = "Quick Add",
                supportingText = "Required field",
            )
        }
    }
}
