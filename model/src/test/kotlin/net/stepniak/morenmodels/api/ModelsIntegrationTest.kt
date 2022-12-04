package net.stepniak.morenmodels.api

import net.stepniak.morenomodels.api.infrastructure.ClientException
import net.stepniak.morenomodels.api.model.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ModelsIntegrationTest : BaseIntegrationTest() {

    @Test
    fun `creates a model`() {
        // given
        val newModel = NewModel(
            modelSlug = "test-model-${UUID.randomUUID()}",
            givenName = "Konrad",
            familyName = "Moreno"
        )

        // when
        val model = api.createModel(newModel)

        // then
        assertNotNull(model.modelId)
        assertNotNull(model.created)
        assertEquals(1, model.version)
        assertEquals("Konrad", model.givenName)
        assertEquals("Moreno", model.familyName)
        assertEquals(newModel.modelSlug, model.modelSlug)
        assertEquals(false, model.archived)
        assertNull(model.updated)
        assertNull(model.eyeColor)
        assertNull(model.height)

        // cleanup
        api.archiveModel(newModel.modelSlug, delete = true)
    }

    @Test
    fun `gets a model`() {
        // given
        val newModel = NewModel(
            modelSlug = "test-model-${UUID.randomUUID()}",
            givenName = "Konrad",
            familyName = "Moreno",
            eyeColor = EyeColor.blue,
            height = 180,
        )
        api.createModel(newModel)

        // when
        val model = api.getModel(newModel.modelSlug)

        // then
        assertNotNull(model.modelId)
        assertNotNull(model.created)
        assertEquals(1, model.version)
        assertEquals(newModel.modelSlug, model.modelSlug)
        assertEquals(newModel.givenName, model.givenName)
        assertEquals(newModel.familyName, model.familyName)
        assertEquals(newModel.eyeColor, model.eyeColor)
        assertEquals(newModel.height, model.height)

        // cleanup
        api.archiveModel(model.modelSlug, true)
    }

    @Test
    fun `updates model`() {
        // given
        val newModel = NewModel(
            modelSlug = "test-model-${UUID.randomUUID()}",
            givenName = "Konrad",
            familyName = "Moreno",
        )
        val model = api.createModel(newModel)
        val updatableModel = UpdatableModel(
            modelSlug = "different-slug-${UUID.randomUUID()}",
            givenName = "Matthew",
            familyName = "Conrad",
            eyeColor = EyeColor.brown,
            height = 181,
            version = model.version,
        )

        // when
        val updatedModel = api.updateModel(model.modelSlug, updatableModel)
        // Optimistic locking
        val ex = assertThrows<ClientException> {
            api.updateModel(model.modelSlug, updatableModel)
        }

        // then
        assertEquals(2, updatedModel.version)
        assertEquals(updatableModel.modelSlug, updatedModel.modelSlug)
        assertEquals(updatableModel.givenName, updatedModel.givenName)
        assertEquals(updatableModel.familyName, updatedModel.familyName)
        assertEquals(updatableModel.eyeColor, updatedModel.eyeColor)
        assertEquals(updatableModel.height, updatedModel.height)
        assertEquals(409, ex.statusCode)

        // cleanup
        api.archiveModel(model.modelSlug, true)
    }

    @Test
    fun `validates create model parameters`() {
        val ex = assertThrows<ClientException> {
            api.createModel(NewModel(
                modelSlug = "",
                familyName = "A".repeat(100),
                givenName = "B".repeat(100),
                eyeColor = EyeColor.brown,
                height = 12345678
            ))
        }
        assertEquals(400, ex.statusCode)
    }

    @Test
    fun `lists models and paginates`() {
        // given
        val pageSize = 5
        val numberOfCreatedModels = 11
        val createdModels = (1..11).map {
            api.createModel(NewModel(
                "test-model-${UUID.randomUUID()}",
                givenName = "Konrad $it",
                familyName = "Moreno ${it * 10}"
            ))
        }.toList()

        var seenPages = 0
        var seenModels = 0
        var models: Models? = null
        // when
        do {
            models = api.listModels(
                nextToken = models?.metadata?.nextToken,
                pageSize,
                showArchived = null,
            );

            seenPages += 1
            seenModels += models.items.size

            models.items.forEach {
                assertNotNull(it.modelId)
                assertNotNull(it.modelSlug)
                assertNull(it.height)
                assertNull(it.eyeColor)
                assertEquals(false, it.archived)
                assertNotNull(it.created)
                assertNull(it.created)
                assertEquals(1, it.version)
            }

        } while (models?.metadata?.nextToken != null)

        // then
        assertTrue {
            seenModels == numberOfCreatedModels
                    && seenPages == 3
        }

        // cleanup
        createdModels.forEach { api.archiveModel(it.modelSlug, true) }
    }

    @Test
    fun `validates list models parameters`() {
        // given, when
        val ex = assertThrows<ClientException> {
            api.listModels(
                nextToken = "3".repeat(513),
                pageSize = 1001,
                showArchived = null
            )
        }
        // then
        assertEquals(400, ex.statusCode)
    }

    @Test
    fun `validates archive model parameters`() {
        val ex = assertThrows<ClientException> {
            api.archiveModel(modelSlug = INVALID_SLUG, delete = false)
        }
        // then
        assertEquals(400, ex.statusCode)
    }

    @Test
    fun `erases a model`() {
        // given
        val model = createModel()

        // when
        api.archiveModel(model.modelSlug, true)

        // then
        assertThrows<Exception> {
            api.getModel(model.modelSlug)
        }
    }

    @Test
    fun `archives a model`() {
        // given
        val model = createModel()

        // when
        api.archiveModel(model.modelSlug, false)

        // then
        assertTrue(api.getModel(model.modelSlug).archived)

        // cleanup
        api.archiveModel(model.modelSlug, true)
    }

    @Test
    fun `properly handles not existing models when getting one`() {
        // given, when
        val ex = assertThrows<ClientException> {
            api.getModel(NON_EXISTENT_SLUG)
        }
        // then
        assertEquals(404, ex.statusCode)
    }

    @Test
    fun `properly handles not existing models deleting getting one`() {
        // given, when
        val ex = assertThrows<ClientException> {
            api.archiveModel(NON_EXISTENT_SLUG, true)
        }
        // then
        assertEquals(404, ex.statusCode)
    }

    private fun createModel(): Model {
        return api.createModel(
            NewModel(
            modelSlug = "test-model-${UUID.randomUUID()}",
            givenName = "Test",
            familyName = "Model"
        )
        )
    }

    companion object {
        private const val INVALID_SLUG = "#4_(*!@#*/"
        private const val NON_EXISTENT_SLUG = "non-ex3st1nt-sl4g"
    }
}