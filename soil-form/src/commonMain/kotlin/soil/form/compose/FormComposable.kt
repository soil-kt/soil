// Copyright 2025 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.form.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.autoSaver
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import soil.form.FieldValidateOn
import soil.form.FormPolicy
import soil.form.FormValidationException


@Composable
fun <T : Any> rememberForm(
    initialValue: T,
    policy: FormPolicy = FormPolicy.Default,
    saver: Saver<T, Any> = autoSaver(),
    key: Any? = null,
    // TODO: rulesがほしい(Fieldで指定する代わりにここでも指定できる => LazyColumnをどうしても使いたい用途に役立つかも)
    //  rules =
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
    onError: ((err: Throwable) -> Unit)? = null,
    // TODO: submit系のstate外せるならsuspend無くせそう
    onSubmit: suspend (formData: T) -> Unit
): FormScope<T> {
    val state = rememberFormState(initialValue, policy, saver, key)
    return remember(state) {
        // TODO: handleSubmitはFormScope内で関数定義したほうがよさそう
        FormScope(state) {
            // TODO: state分けたほうがre-composition減る？
            if (!state.isSubmitting) {
                val formValue = state.value
                val submissionPolicy = policy.submission
                state.isSubmitting = true
                coroutineScope.launch {
                    var isCompleted = false
                    try {
                        if (submissionPolicy.validate(formValue, state.rules, false)) {
                            onSubmit(formValue)
                            isCompleted = true
                        }
                    } catch (e: FormValidationException) {
                        state.forceError(e.errors, FieldValidateOn.Submit)
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Throwable) {
                        onError?.invoke(e)
                    } finally {
                        state.isSubmitting = false
                        // TODO: query機能(mutation)の責務にしちゃってもよさそう
                        //  破棄ケースだと結局落ちる
                        state.isSubmitted = isCompleted
                        state.submitCount += 1
                    }
                }
            }
        }
    }
}
