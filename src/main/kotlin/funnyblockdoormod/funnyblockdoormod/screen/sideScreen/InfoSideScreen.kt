package funnyblockdoormod.funnyblockdoormod.screen.sideScreen

import funnyblockdoormod.funnyblockdoormod.screen.DoorEmitterScreen
import funnyblockdoormod.funnyblockdoormod.screen.DoorEmitterScreenHandler
import funnyblockdoormod.funnyblockdoormod.screen.expandableScreen.ExpandableScreen

class InfoSideScreen: ExpandableScreen {

    companion object: Factory {
        override fun createScreen(parent: DoorEmitterScreen, handler: DoorEmitterScreenHandler, tabIdx: Int): ExpandableScreen {
            return InfoSideScreen(parent, handler, tabIdx)
        }
    }
}