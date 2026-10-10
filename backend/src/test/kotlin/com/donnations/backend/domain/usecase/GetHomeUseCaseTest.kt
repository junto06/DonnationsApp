package com.donnations.backend.domain.usecase

import com.donnations.backend.data.campaigns.CampaignsRepositoryImpl
import com.donnations.backend.data.campaigns.store.InMemoryCampaignsStore
import com.donnations.backend.data.categories.CategoriesRepositoryImpl
import com.donnations.backend.data.categories.store.InMemoryCategoriesStore
import com.donnations.backend.domain.campaign
import com.donnations.backend.domain.model.Category
import com.donnations.backend.domain.model.CategoryId
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals

class GetHomeUseCaseTest {

    private val oldFeatured = campaign(title = "Old featured", featured = true, createdAt = Instant.parse("2026-09-01T00:00:00Z"))
    private val newFeatured = campaign(title = "New featured", featured = true, createdAt = Instant.parse("2026-10-01T00:00:00Z"))
    private val oldRegular = campaign(title = "Old regular", createdAt = Instant.parse("2026-09-15T00:00:00Z"))
    private val newRegular = campaign(title = "New regular", createdAt = Instant.parse("2026-10-15T00:00:00Z"))
    private val categories = listOf(Category(CategoryId("health"), "Health"), Category(CategoryId("education"), "Education"))

    private val getHome = GetHomeUseCase(
        CampaignsRepositoryImpl(InMemoryCampaignsStore(listOf(oldFeatured, oldRegular, newRegular, newFeatured))),
        CategoriesRepositoryImpl(InMemoryCategoriesStore(categories)),
    )

    @Test
    fun `featured campaigns go to the carousel newest first`() {
        assertEquals(listOf(newFeatured, oldFeatured), getHome().featured)
    }

    @Test
    fun `remaining campaigns exclude featured ones newest first`() {
        assertEquals(listOf(newRegular, oldRegular), getHome().campaigns)
    }

    @Test
    fun `categories keep their configured order`() {
        assertEquals(categories, getHome().categories)
    }
}
