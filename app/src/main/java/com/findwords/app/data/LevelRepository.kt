package com.findwords.app.data

import com.findwords.app.model.WordPack
import com.findwords.app.engine.WordSearchGenerator
import com.google.gson.Gson

object LevelRepository {

    private val packs = listOf(
        WordPack(1, "Animals", "animals", listOf(
            listOf("LION", "TIGER", "BEAR", "WOLF", "FOX", "DEER", "HARE", "MOLE"),
            listOf("ELEPHANT", "GIRAFFE", "ZEBRA", "RHINO", "HIPPO", "HYENA", "CHEETAH", "LEOPARD"),
            listOf("MONKEY", "GORILLA", "CHIMP", "ORANGUTAN", "BABOON", "MACAQUE", "LEMUR", "TAMARIN"),
            listOf("PENGUIN", "DOLPHIN", "WHALE", "SHARK", "SEAL", "OTTER", "WALRUS", "MANATEE"),
            listOf("EAGLE", "HAWK", "OWL", "FALCON", "VULTURE", "CONDOR", "ROBIN", "SPARROW"),
            listOf("SNAKE", "PYTHON", "COBRA", "VIPER", "MAMBA", "BOA", "ADDER", "RACER"),
            listOf("FROG", "TOAD", "NEWT", "SALAMANDER", "AXOLOTL", "CAECILIAN", "TREEFROG", "BULLFROG"),
            listOf("BUTTERFLY", "MOTH", "BEETLE", "ANT", "BEE", "WASP", "DRAGONFLY", "LADYBUG"),
            listOf("SQUID", "OCTOPUS", "CRAB", "LOBSTER", "SHRIMP", "BARNACLE", "MUSSEL", "CLAM"),
            listOf("KOALA", "KANGAROO", "WOMBAT", "WALLABY", "POSSUM", "BANDICOOT", "QUOKKA", "NUMBAT")
        ), 10, 1),

        WordPack(2, "Food & Drink", "food", listOf(
            listOf("PIZZA", "BURGER", "PASTA", "SUSHI", "TACO", "SALAD", "STEAK", "RICE"),
            listOf("BREAD", "CHEESE", "BUTTER", "MILK", "EGGS", "YOGURT", "CREAM", "HONEY"),
            listOf("APPLE", "BANANA", "ORANGE", "GRAPE", "MANGO", "PEACH", "PEAR", "PLUM"),
            listOf("CARROT", "POTATO", "ONION", "TOMATO", "PEPPER", "BROCCOLI", "SPINACH", "CORN"),
            listOf("CHICKEN", "BEEF", "PORK", "LAMB", "TURKEY", "DUCK", "HAM", "BACON"),
            listOf("COFFEE", "TEA", "JUICE", "WATER", "SODA", "WINE", "BEER", "MILK"),
            listOf("CAKE", "PIE", "COOKIE", "BROWNIE", "CANDY", "CHOCOLATE", "ICECREAM", "PUDDING"),
            listOf("SOUP", "STEW", "CURRY", "CHILI", "RAMEN", "PHO", "BISQUE", "GAZPACHO"),
            listOf("SANDWICH", "WRAP", "BAGEL", "TOAST", "PANINI", "BURRITO", "QUESADILLA", "KEBAB"),
            listOf("SPICE", "SALT", "PEPPER", "GARLIC", "GINGER", "BASIL", "OREGANO", "THYME")
        ), 10, 2),

        WordPack(3, "Countries", "geography", listOf(
            listOf("USA", "CANADA", "MEXICO", "BRAZIL", "ARGENTINA", "CHILE", "PERU", "COLOMBIA"),
            listOf("FRANCE", "GERMANY", "ITALY", "SPAIN", "PORTUGAL", "GREECE", "POLAND", "SWEDEN"),
            listOf("CHINA", "JAPAN", "KOREA", "INDIA", "THAILAND", "VIETNAM", "INDONESIA", "MALAYSIA"),
            listOf("AUSTRALIA", "ZEALAND", "FIJI", "SAMOA", "TONGA", "PALAU", "NAURU", "TUVALU"),
            listOf("EGYPT", "KENYA", "NIGERIA", "GHANA", "MOROCCO", "TUNISIA", "ALGERIA", "LIBYA"),
            listOf("RUSSIA", "UKRAINE", "BELARUS", "MOLDOVA", "LATVIA", "LITHUANIA", "ESTONIA", "POLAND"),
            listOf("TURKEY", "IRAN", "IRAQ", "SYRIA", "JORDAN", "LEBANON", "ISRAEL", "YEMEN"),
            listOf("MEXICO", "CUBA", "HAITI", "JAMAICA", "DOMINICA", "TRINIDAD", "BARBADOS", "GRENADA"),
            listOf("ARGENTINA", "CHILE", "URUGUAY", "PARAGUAY", "BOLIVIA", "ECUADOR", "VENEZUELA", "GUYANA"),
            listOf("SOUTHAFRICA", "NAMIBIA", "BOTSWANA", "ZIMBABWE", "ZAMBIA", "MALAWI", "MOZAMBIQUE", "ANGOLA")
        ), 10, 2),

        WordPack(4, "Sports", "sports", listOf(
            listOf("FOOTBALL", "SOCCER", "BASKETBALL", "BASEBALL", "TENNIS", "GOLF", "HOCKEY", "RUGBY"),
            listOf("SWIMMING", "RUNNING", "CYCLING", "BOXING", "WRESTLING", "JUDO", "KARATE", "FENCING"),
            listOf("VOLLEYBALL", "BADMINTON", "SQUASH", "TABLETENNIS", "HANDBALL", "WATERPOLO", "CRICKET", "LACROSSE"),
            listOf("SKIING", "SNOWBOARD", "SKATING", "SLEDDING", "CURLING", "BIATHLON", "LUGE", "SKELETON"),
            listOf("SURFING", "SAILING", "ROWING", "CANOEING", "KAYAKING", "DIVING", "SNORKELING", "WINDSURF"),
            listOf("GYMNASTICS", "CHEERLEADING", "DANCE", "BALLET", "YOGA", "PILATES", "AEROBICS", "CALISTHENICS"),
            listOf("ARCHERY", "SHOOTING", "DARTS", "BILLIARDS", "BOWLING", "CURLING", "BOCCE", "PETANQUE"),
            listOf("TRIATHLON", "MARATHON", "SPRINT", "HURDLES", "JAVELIN", "DISCUS", "SHOTPUT", "POLEVAULT"),
            listOf("WEIGHTLIFTING", "POWERLIFTING", "BODYBUILDING", "STRONGMAN", "CROSSFIT", "CALISTHENICS", "STREETWORKOUT", "PARKOUR"),
            listOf("ESPORT", "GAMING", "CHESS", "POKER", "BRIDGE", "BACKGAMMON", "GO", "SHOGI")
        ), 10, 3),

        WordPack(5, "Science & Nature", "science", listOf(
            listOf("ATOM", "MOLECULE", "ELEMENT", "COMPOUND", "REACTION", "ENERGY", "MATTER", "FORCE"),
            listOf("GRAVITY", "MAGNETISM", "ELECTRICITY", "LIGHT", "SOUND", "HEAT", "PRESSURE", "VACUUM"),
            listOf("PLANET", "STAR", "GALAXY", "NEBULA", "COMET", "ASTEROID", "METEOR", "BLACKHOLE"),
            listOf("DNA", "GENE", "CHROMOSOME", "CELL", "ORGANISM", "EVOLUTION", "NATURAL", "SELECTION"),
            listOf("PHOTOSYNTHESIS", "RESPIRATION", "METABOLISM", "ENZYME", "PROTEIN", "VITAMIN", "MINERAL", "HORMONE"),
            listOf("ECOSYSTEM", "HABITAT", "BIODIVERSITY", "EXTINCTION", "CONSERVATION", "CLIMATE", "WEATHER", "ATMOSPHERE"),
            listOf("VOLCANO", "EARTHQUAKE", "TSUNAMI", "HURRICANE", "TORNADO", "AVALANCHE", "LANDSLIDE", "FLOOD"),
            listOf("PERIODIC", "TABLE", "NOBLE", "GAS", "METAL", "NONMETAL", "ISOTOPE", "RADIOACTIVE"),
            listOf("QUANTUM", "RELATIVITY", "PHYSICS", "CHEMISTRY", "BIOLOGY", "ASTRONOMY", "GEOLOGY", "ECOLOGY"),
            listOf("TELESCOPE", "MICROSCOPE", "SATELLITE", "ROCKET", "ROVER", "PROBE", "OBSERVATORY", "LABORATORY")
        ), 11, 3)
    )

    fun getAllPacks(): List<WordPack> = packs

    fun getPack(id: Int): WordPack? = packs.find { it.id == id }

    fun generateAllLevels(): List<GeneratedLevel> {
        val levels = mutableListOf<GeneratedLevel>()
        for (pack in packs) {
            for (i in pack.wordLists.indices) {
                val words = pack.wordLists[i]
                val result = WordSearchGenerator.generate(words, pack.gridSize)
                levels.add(GeneratedLevel(
                    id = levels.size + 1,
                    packId = pack.id,
                    packName = pack.name,
                    levelIndex = i + 1,
                    words = result.words.map { it.word },
                    grid = result.grid,
                    gridSize = pack.gridSize,
                    difficulty = pack.difficulty
                ))
            }
        }
        return levels
    }

    data class GeneratedLevel(
        val id: Int,
        val packId: Int,
        val packName: String,
        val levelIndex: Int,
        val words: List<String>,
        val grid: Array<CharArray>,
        val gridSize: Int,
        val difficulty: Int
    )
}