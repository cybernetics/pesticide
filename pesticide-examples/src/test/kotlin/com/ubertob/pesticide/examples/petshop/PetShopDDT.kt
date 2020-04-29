package com.ubertob.pesticide.examples.petshop

import com.ubertob.pesticide.DDT
import com.ubertob.pesticide.DomainDrivenTest
import com.ubertob.pesticide.InMemoryHubs
import com.ubertob.pesticide.NamedActor
import java.time.LocalDate


class PetShopDDT : DomainDrivenTest<PetShopDomainWrapper>(allPetShopAbstractions()) {

    val mary by NamedActor(::PetBuyer)

    @DDT
    fun `mary buys a lamb`() = ddtScenario {
        val lamb = Pet("lamb", 64)
        val hamster = Pet("hamster", 128)
        setting {
            populateShop(lamb, hamster)
        } atRise play(
            mary.`check that the price of $ is $`("lamb", 64),
            mary.`check that the price of $ is $`("hamster", 128),
            mary.`buy a $`("lamb")
        ).wip(LocalDate.of(2020, 5, 30), "Complete domain and actors", setOf(InMemoryHubs::class))
    }


}


