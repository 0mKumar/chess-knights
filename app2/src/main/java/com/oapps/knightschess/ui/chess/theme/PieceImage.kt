package com.oapps.knightschess.ui.chess.theme

import com.oapps.knightschess.R


sealed class Image {
    abstract val type: String
    abstract val bk: Int
    abstract val bq: Int
    abstract val br: Int
    abstract val bb: Int
    abstract val bn: Int
    abstract val bp: Int
    abstract val wk: Int
    abstract val wq: Int
    abstract val wr: Int
    abstract val wb: Int
    abstract val wn: Int
    abstract val wp: Int

    enum class Type{
        CARDINAL,
        GIOCO,
        TATIANA,
        ALPHA,
        STAUNTY,
        LEIPZIG,
        LETTER,
        PIROUETTI,
        FRESCA,
        CBURNETT,
        MERIDA,
        RIOHACHA,
        MAESTRO,
        CHESSNUT,
        SPATIAL,
        DUBROVNY,
        KOSAL,
        LIBRA,
        FANTASY,
        REILLYCRAIG,
        HORSEY,
        CALIFORNIA,
        SHAPES,
        PIXEL,
        GOVERNOR,
        COMPANION,
        ICPIECES,
        CHESS7,
    }

    companion object Comp{
        fun from(type: Type) = when(type){
            Type.CARDINAL -> Cardinal
            Type.GIOCO -> Gioco
            Type.TATIANA -> Tatiana
            Type.ALPHA -> Alpha
            Type.STAUNTY -> Staunty
            Type.LEIPZIG -> Leipzig
            Type.LETTER -> Letter
            Type.PIROUETTI -> Pirouetti
            Type.FRESCA -> Fresca
            Type.CBURNETT -> Cburnett
            Type.MERIDA -> Merida
            Type.RIOHACHA -> Riohacha
            Type.MAESTRO -> Maestro
            Type.CHESSNUT -> Chessnut
            Type.SPATIAL -> Spatial
            Type.DUBROVNY -> Dubrovny
            Type.KOSAL -> Kosal
            Type.LIBRA -> Libra
            Type.FANTASY -> Fantasy
            Type.REILLYCRAIG -> Reillycraig
            Type.HORSEY -> Horsey
            Type.CALIFORNIA -> California
            Type.SHAPES -> Shapes
            Type.PIXEL -> Pixel
            Type.GOVERNOR -> Governor
            Type.COMPANION -> Companion
            Type.ICPIECES -> Icpieces
            Type.CHESS7 -> Chess7
        }
    }


    operator fun get(kind: Char) = when(kind) {
        'k' -> bk
        'q' -> bq
        'r' -> br
        'b' -> bb
        'n' -> bn
        'p' -> bp
        'K' -> wk
        'Q' -> wq
        'R' -> wr
        'B' -> wb
        'N' -> wn
        'P' -> wp
        else -> bn
    }

    object Cardinal: Image() {
        override val type = "Cardinal"
        override val bk = R.drawable.cardinal_bk
        override val bq = R.drawable.cardinal_bq
        override val br = R.drawable.cardinal_br
        override val bb = R.drawable.cardinal_bb
        override val bn = R.drawable.cardinal_bn
        override val bp = R.drawable.cardinal_bp
        override val wk = R.drawable.cardinal_wk
        override val wq = R.drawable.cardinal_wq
        override val wr = R.drawable.cardinal_wr
        override val wb = R.drawable.cardinal_wb
        override val wn = R.drawable.cardinal_wn
        override val wp = R.drawable.cardinal_wp
    }

    object Gioco: Image() {
        override val type = "Gioco"
        override val bk = R.drawable.gioco_bk
        override val bq = R.drawable.gioco_bq
        override val br = R.drawable.gioco_br
        override val bb = R.drawable.gioco_bb
        override val bn = R.drawable.gioco_bn
        override val bp = R.drawable.gioco_bp
        override val wk = R.drawable.gioco_wk
        override val wq = R.drawable.gioco_wq
        override val wr = R.drawable.gioco_wr
        override val wb = R.drawable.gioco_wb
        override val wn = R.drawable.gioco_wn
        override val wp = R.drawable.gioco_wp
    }

    object Tatiana: Image() {
        override val type = "Tatiana"
        override val bk = R.drawable.tatiana_bk
        override val bq = R.drawable.tatiana_bq
        override val br = R.drawable.tatiana_br
        override val bb = R.drawable.tatiana_bb
        override val bn = R.drawable.tatiana_bn
        override val bp = R.drawable.tatiana_bp
        override val wk = R.drawable.tatiana_wk
        override val wq = R.drawable.tatiana_wq
        override val wr = R.drawable.tatiana_wr
        override val wb = R.drawable.tatiana_wb
        override val wn = R.drawable.tatiana_wn
        override val wp = R.drawable.tatiana_wp
    }

    object Alpha: Image() {
        override val type = "Alpha"
        override val bk = R.drawable.alpha_bk
        override val bq = R.drawable.alpha_bq
        override val br = R.drawable.alpha_br
        override val bb = R.drawable.alpha_bb
        override val bn = R.drawable.alpha_bn
        override val bp = R.drawable.alpha_bp
        override val wk = R.drawable.alpha_wk
        override val wq = R.drawable.alpha_wq
        override val wr = R.drawable.alpha_wr
        override val wb = R.drawable.alpha_wb
        override val wn = R.drawable.alpha_wn
        override val wp = R.drawable.alpha_wp
    }

    object Staunty: Image() {
        override val type = "Staunty"
        override val bk = R.drawable.staunty_bk
        override val bq = R.drawable.staunty_bq
        override val br = R.drawable.staunty_br
        override val bb = R.drawable.staunty_bb
        override val bn = R.drawable.staunty_bn
        override val bp = R.drawable.staunty_bp
        override val wk = R.drawable.staunty_wk
        override val wq = R.drawable.staunty_wq
        override val wr = R.drawable.staunty_wr
        override val wb = R.drawable.staunty_wb
        override val wn = R.drawable.staunty_wn
        override val wp = R.drawable.staunty_wp
    }

    object Leipzig: Image() {
        override val type = "Leipzig"
        override val bk = R.drawable.leipzig_bk
        override val bq = R.drawable.leipzig_bq
        override val br = R.drawable.leipzig_br
        override val bb = R.drawable.leipzig_bb
        override val bn = R.drawable.leipzig_bn
        override val bp = R.drawable.leipzig_bp
        override val wk = R.drawable.leipzig_wk
        override val wq = R.drawable.leipzig_wq
        override val wr = R.drawable.leipzig_wr
        override val wb = R.drawable.leipzig_wb
        override val wn = R.drawable.leipzig_wn
        override val wp = R.drawable.leipzig_wp
    }

    object Letter: Image() {
        override val type = "Letter"
        override val bk = R.drawable.letter_bk
        override val bq = R.drawable.letter_bq
        override val br = R.drawable.letter_br
        override val bb = R.drawable.letter_bb
        override val bn = R.drawable.letter_bn
        override val bp = R.drawable.letter_bp
        override val wk = R.drawable.letter_wk
        override val wq = R.drawable.letter_wq
        override val wr = R.drawable.letter_wr
        override val wb = R.drawable.letter_wb
        override val wn = R.drawable.letter_wn
        override val wp = R.drawable.letter_wp
    }

    object Pirouetti: Image() {
        override val type = "Pirouetti"
        override val bk = R.drawable.pirouetti_bk
        override val bq = R.drawable.pirouetti_bq
        override val br = R.drawable.pirouetti_br
        override val bb = R.drawable.pirouetti_bb
        override val bn = R.drawable.pirouetti_bn
        override val bp = R.drawable.pirouetti_bp
        override val wk = R.drawable.pirouetti_wk
        override val wq = R.drawable.pirouetti_wq
        override val wr = R.drawable.pirouetti_wr
        override val wb = R.drawable.pirouetti_wb
        override val wn = R.drawable.pirouetti_wn
        override val wp = R.drawable.pirouetti_wp
    }

    object Fresca: Image() {
        override val type = "Fresca"
        override val bk = R.drawable.fresca_bk
        override val bq = R.drawable.fresca_bq
        override val br = R.drawable.fresca_br
        override val bb = R.drawable.fresca_bb
        override val bn = R.drawable.fresca_bn
        override val bp = R.drawable.fresca_bp
        override val wk = R.drawable.fresca_wk
        override val wq = R.drawable.fresca_wq
        override val wr = R.drawable.fresca_wr
        override val wb = R.drawable.fresca_wb
        override val wn = R.drawable.fresca_wn
        override val wp = R.drawable.fresca_wp
    }

    object Cburnett: Image() {
        override val type = "Cburnett"
        override val bk = R.drawable.cburnett_bk
        override val bq = R.drawable.cburnett_bq
        override val br = R.drawable.cburnett_br
        override val bb = R.drawable.cburnett_bb
        override val bn = R.drawable.cburnett_bn
        override val bp = R.drawable.cburnett_bp
        override val wk = R.drawable.cburnett_wk
        override val wq = R.drawable.cburnett_wq
        override val wr = R.drawable.cburnett_wr
        override val wb = R.drawable.cburnett_wb
        override val wn = R.drawable.cburnett_wn
        override val wp = R.drawable.cburnett_wp
    }

    object Merida: Image() {
        override val type = "Merida"
        override val bk = R.drawable.merida_bk
        override val bq = R.drawable.merida_bq
        override val br = R.drawable.merida_br
        override val bb = R.drawable.merida_bb
        override val bn = R.drawable.merida_bn
        override val bp = R.drawable.merida_bp
        override val wk = R.drawable.merida_wk
        override val wq = R.drawable.merida_wq
        override val wr = R.drawable.merida_wr
        override val wb = R.drawable.merida_wb
        override val wn = R.drawable.merida_wn
        override val wp = R.drawable.merida_wp
    }

    object Riohacha: Image() {
        override val type = "Riohacha"
        override val bk = R.drawable.riohacha_bk
        override val bq = R.drawable.riohacha_bq
        override val br = R.drawable.riohacha_br
        override val bb = R.drawable.riohacha_bb
        override val bn = R.drawable.riohacha_bn
        override val bp = R.drawable.riohacha_bp
        override val wk = R.drawable.riohacha_wk
        override val wq = R.drawable.riohacha_wq
        override val wr = R.drawable.riohacha_wr
        override val wb = R.drawable.riohacha_wb
        override val wn = R.drawable.riohacha_wn
        override val wp = R.drawable.riohacha_wp
    }

    object Maestro: Image() {
        override val type = "Maestro"
        override val bk = R.drawable.maestro_bk
        override val bq = R.drawable.maestro_bq
        override val br = R.drawable.maestro_br
        override val bb = R.drawable.maestro_bb
        override val bn = R.drawable.maestro_bn
        override val bp = R.drawable.maestro_bp
        override val wk = R.drawable.maestro_wk
        override val wq = R.drawable.maestro_wq
        override val wr = R.drawable.maestro_wr
        override val wb = R.drawable.maestro_wb
        override val wn = R.drawable.maestro_wn
        override val wp = R.drawable.maestro_wp
    }

    object Chessnut: Image() {
        override val type = "Chessnut"
        override val bk = R.drawable.chessnut_bk
        override val bq = R.drawable.chessnut_bq
        override val br = R.drawable.chessnut_br
        override val bb = R.drawable.chessnut_bb
        override val bn = R.drawable.chessnut_bn
        override val bp = R.drawable.chessnut_bp
        override val wk = R.drawable.chessnut_wk
        override val wq = R.drawable.chessnut_wq
        override val wr = R.drawable.chessnut_wr
        override val wb = R.drawable.chessnut_wb
        override val wn = R.drawable.chessnut_wn
        override val wp = R.drawable.chessnut_wp
    }

    object Spatial: Image() {
        override val type = "Spatial"
        override val bk = R.drawable.spatial_bk
        override val bq = R.drawable.spatial_bq
        override val br = R.drawable.spatial_br
        override val bb = R.drawable.spatial_bb
        override val bn = R.drawable.spatial_bn
        override val bp = R.drawable.spatial_bp
        override val wk = R.drawable.spatial_wk
        override val wq = R.drawable.spatial_wq
        override val wr = R.drawable.spatial_wr
        override val wb = R.drawable.spatial_wb
        override val wn = R.drawable.spatial_wn
        override val wp = R.drawable.spatial_wp
    }

    object Dubrovny: Image() {
        override val type = "Dubrovny"
        override val bk = R.drawable.dubrovny_bk
        override val bq = R.drawable.dubrovny_bq
        override val br = R.drawable.dubrovny_br
        override val bb = R.drawable.dubrovny_bb
        override val bn = R.drawable.dubrovny_bn
        override val bp = R.drawable.dubrovny_bp
        override val wk = R.drawable.dubrovny_wk
        override val wq = R.drawable.dubrovny_wq
        override val wr = R.drawable.dubrovny_wr
        override val wb = R.drawable.dubrovny_wb
        override val wn = R.drawable.dubrovny_wn
        override val wp = R.drawable.dubrovny_wp
    }

    object Kosal: Image() {
        override val type = "Kosal"
        override val bk = R.drawable.kosal_bk
        override val bq = R.drawable.kosal_bq
        override val br = R.drawable.kosal_br
        override val bb = R.drawable.kosal_bb
        override val bn = R.drawable.kosal_bn
        override val bp = R.drawable.kosal_bp
        override val wk = R.drawable.kosal_wk
        override val wq = R.drawable.kosal_wq
        override val wr = R.drawable.kosal_wr
        override val wb = R.drawable.kosal_wb
        override val wn = R.drawable.kosal_wn
        override val wp = R.drawable.kosal_wp
    }

    object Libra: Image() {
        override val type = "Libra"
        override val bk = R.drawable.libra_bk
        override val bq = R.drawable.libra_bq
        override val br = R.drawable.libra_br
        override val bb = R.drawable.libra_bb
        override val bn = R.drawable.libra_bn
        override val bp = R.drawable.libra_bp
        override val wk = R.drawable.libra_wk
        override val wq = R.drawable.libra_wq
        override val wr = R.drawable.libra_wr
        override val wb = R.drawable.libra_wb
        override val wn = R.drawable.libra_wn
        override val wp = R.drawable.libra_wp
    }

    object Fantasy: Image() {
        override val type = "Fantasy"
        override val bk = R.drawable.fantasy_bk
        override val bq = R.drawable.fantasy_bq
        override val br = R.drawable.fantasy_br
        override val bb = R.drawable.fantasy_bb
        override val bn = R.drawable.fantasy_bn
        override val bp = R.drawable.fantasy_bp
        override val wk = R.drawable.fantasy_wk
        override val wq = R.drawable.fantasy_wq
        override val wr = R.drawable.fantasy_wr
        override val wb = R.drawable.fantasy_wb
        override val wn = R.drawable.fantasy_wn
        override val wp = R.drawable.fantasy_wp
    }

    object Reillycraig: Image() {
        override val type = "Reillycraig"
        override val bk = R.drawable.reillycraig_bk
        override val bq = R.drawable.reillycraig_bq
        override val br = R.drawable.reillycraig_br
        override val bb = R.drawable.reillycraig_bb
        override val bn = R.drawable.reillycraig_bn
        override val bp = R.drawable.reillycraig_bp
        override val wk = R.drawable.reillycraig_wk
        override val wq = R.drawable.reillycraig_wq
        override val wr = R.drawable.reillycraig_wr
        override val wb = R.drawable.reillycraig_wb
        override val wn = R.drawable.reillycraig_wn
        override val wp = R.drawable.reillycraig_wp
    }

    object Horsey: Image() {
        override val type = "Horsey"
        override val bk = R.drawable.horsey_bk
        override val bq = R.drawable.horsey_bq
        override val br = R.drawable.horsey_br
        override val bb = R.drawable.horsey_bb
        override val bn = R.drawable.horsey_bn
        override val bp = R.drawable.horsey_bp
        override val wk = R.drawable.horsey_wk
        override val wq = R.drawable.horsey_wq
        override val wr = R.drawable.horsey_wr
        override val wb = R.drawable.horsey_wb
        override val wn = R.drawable.horsey_wn
        override val wp = R.drawable.horsey_wp
    }

    object California: Image() {
        override val type = "California"
        override val bk = R.drawable.california_bk
        override val bq = R.drawable.california_bq
        override val br = R.drawable.california_br
        override val bb = R.drawable.california_bb
        override val bn = R.drawable.california_bn
        override val bp = R.drawable.california_bp
        override val wk = R.drawable.california_wk
        override val wq = R.drawable.california_wq
        override val wr = R.drawable.california_wr
        override val wb = R.drawable.california_wb
        override val wn = R.drawable.california_wn
        override val wp = R.drawable.california_wp
    }

    object Shapes: Image() {
        override val type = "Shapes"
        override val bk = R.drawable.shapes_bk
        override val bq = R.drawable.shapes_bq
        override val br = R.drawable.shapes_br
        override val bb = R.drawable.shapes_bb
        override val bn = R.drawable.shapes_bn
        override val bp = R.drawable.shapes_bp
        override val wk = R.drawable.shapes_wk
        override val wq = R.drawable.shapes_wq
        override val wr = R.drawable.shapes_wr
        override val wb = R.drawable.shapes_wb
        override val wn = R.drawable.shapes_wn
        override val wp = R.drawable.shapes_wp
    }

    object Pixel: Image() {
        override val type = "Pixel"
        override val bk = R.drawable.pixel_bk
        override val bq = R.drawable.pixel_bq
        override val br = R.drawable.pixel_br
        override val bb = R.drawable.pixel_bb
        override val bn = R.drawable.pixel_bn
        override val bp = R.drawable.pixel_bp
        override val wk = R.drawable.pixel_wk
        override val wq = R.drawable.pixel_wq
        override val wr = R.drawable.pixel_wr
        override val wb = R.drawable.pixel_wb
        override val wn = R.drawable.pixel_wn
        override val wp = R.drawable.pixel_wp
    }

    object Governor: Image() {
        override val type = "Governor"
        override val bk = R.drawable.governor_bk
        override val bq = R.drawable.governor_bq
        override val br = R.drawable.governor_br
        override val bb = R.drawable.governor_bb
        override val bn = R.drawable.governor_bn
        override val bp = R.drawable.governor_bp
        override val wk = R.drawable.governor_wk
        override val wq = R.drawable.governor_wq
        override val wr = R.drawable.governor_wr
        override val wb = R.drawable.governor_wb
        override val wn = R.drawable.governor_wn
        override val wp = R.drawable.governor_wp
    }

    object Companion: Image() {
        override val type = "Companion"
        override val bk = R.drawable.companion_bk
        override val bq = R.drawable.companion_bq
        override val br = R.drawable.companion_br
        override val bb = R.drawable.companion_bb
        override val bn = R.drawable.companion_bn
        override val bp = R.drawable.companion_bp
        override val wk = R.drawable.companion_wk
        override val wq = R.drawable.companion_wq
        override val wr = R.drawable.companion_wr
        override val wb = R.drawable.companion_wb
        override val wn = R.drawable.companion_wn
        override val wp = R.drawable.companion_wp
    }

    object Icpieces: Image() {
        override val type = "Icpieces"
        override val bk = R.drawable.icpieces_bk
        override val bq = R.drawable.icpieces_bq
        override val br = R.drawable.icpieces_br
        override val bb = R.drawable.icpieces_bb
        override val bn = R.drawable.icpieces_bn
        override val bp = R.drawable.icpieces_bp
        override val wk = R.drawable.icpieces_wk
        override val wq = R.drawable.icpieces_wq
        override val wr = R.drawable.icpieces_wr
        override val wb = R.drawable.icpieces_wb
        override val wn = R.drawable.icpieces_wn
        override val wp = R.drawable.icpieces_wp
    }

    object Chess7: Image() {
        override val type = "Chess7"
        override val bk = R.drawable.chess7_bk
        override val bq = R.drawable.chess7_bq
        override val br = R.drawable.chess7_br
        override val bb = R.drawable.chess7_bb
        override val bn = R.drawable.chess7_bn
        override val bp = R.drawable.chess7_bp
        override val wk = R.drawable.chess7_wk
        override val wq = R.drawable.chess7_wq
        override val wr = R.drawable.chess7_wr
        override val wb = R.drawable.chess7_wb
        override val wn = R.drawable.chess7_wn
        override val wp = R.drawable.chess7_wp
    }
}