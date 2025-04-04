// Copyright 2025 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.form.rule

import soil.form.core.ValidationResult
import soil.testing.UnitTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ObjectRuleTest : UnitTest() {

    @Test
    fun rule_equalTo() {
        val rule = testRule {
            equalTo({ Person() }) { "Invalid!" }
        }
        assertEquals(ValidationResult.Valid, rule(Person()))
        assertEquals(ValidationResult.Invalid("Invalid!"), rule(Person(name = "Bob")))
    }

    @Test
    fun rule_satisfy() {
        val rule = testRule {
            satisfy({ name.isNotBlank() }) { "Invalid!" }
        }
        assertEquals(ValidationResult.Valid, rule(Person()))
        assertEquals(ValidationResult.Invalid("Invalid!"), rule(Person(name = "   ")))
    }

    @Test
    fun rule_cast() {
        val rule = testRule {
            cast { it.name } then {
                notBlank { "Invalid!" }
            }
        }
        assertEquals(ValidationResult.Valid, rule(Person()))
        assertEquals(ValidationResult.Invalid("Invalid!"), rule(Person(name = "")))
    }

    @Test
    fun rule_custom() {
        val custom = ObjectRule<Person>({ age >= 18 && name.isNotBlank() }) { "Invalid!" }
        val rule = testRule {
            extend(custom)
        }
        assertEquals(ValidationResult.Valid, rule(Person()))
        assertEquals(ValidationResult.Invalid("Invalid!"), rule(Person(age = 17)))
        assertEquals(ValidationResult.Invalid("Invalid!"), rule(Person(name = "")))
    }

    @Test
    fun rule_complex_validation() {
        val rule = testRule {
            satisfy({ name.isNotBlank() }) { "name" }
            cast { it.age } then {
                minimum(18) { "age" }
            }
            cast { it.email.lowercase() } then {
                match(".*@.*\\..*") { "email" }
            }
        }
        assertEquals(ValidationResult.Valid, rule(Person()))
        assertEquals(ValidationResult.Invalid("name"), rule(Person(name = "")))
        assertEquals(ValidationResult.Invalid("age"), rule(Person(age = 17)))
        assertEquals(ValidationResult.Invalid("email"), rule(Person(email = "invalid-email")))
    }

    private fun testRule(block: ObjectRuleBuilder<Person>.() -> Unit): ObjectRule<Person> = createTestRule(block)

    private data class Person(
        val name: String = "Alice",
        val age: Int = 18,
        val email: String = "alice@example.com"
    )
}
