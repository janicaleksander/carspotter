package com.example.carspotter.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase



val SEED_CATEGORIES = listOf("HYPER", "ELECTRIC", "TRACK", "OLD","OTHER")
val SEED_BRANDS = listOf(
    "LAMBORGHINI", "FERRARI", "PORSCHE", "BUGATTI", "MCLAREN",
    "MERCEDES-BENZ", "AUDI", "BMW", "TESLA", "FORD",
    "CHEVROLET", "NISSAN", "TOYOTA", "HONDA", "VOLKSWAGEN",
    "HYUNDAI", "KIA", "SUBARU", "MAZDA", "JAGUAR",
    "LEXUS", "INFINITI", "ACURA", "ALFA ROMEO", "FIAT",
    "MITSUBISHI", "PEUGEOT", "RENAULT", "SEAT", "SKODA",
    "VOLVO", "CITROEN", "LAND ROVER", "ROLLS-ROYCE", "BENTLEY",
    "ASTON MARTIN","KOENIGSEGG",
    "RIMAC", "PAGANI", "HENNESSEY",
    "PININFARINA", "SSC", "CZINGER",
    "GORDON MURRAY","OTHER"
)

fun seedTopCars(db: SupportSQLiteDatabase) {

    db.execSQL("""
        INSERT INTO car (brand, model, year, price, description, category, isTop, powerHP, acceleration, maxSpeed) VALUES
        ("PORSCHE", "918 Spyder", 2019, 845000.0, "A hybrid hypercar inspired by Formula 1 technology, combining a naturally aspirated V8 engine with electric motors to deliver extreme performance and efficiency. One of Porsche’s most iconic modern vehicles.", "HYPER", 1, 887, 2.6, 345.0),

        ("BUGATTI", "Chiron Super Sport", 2023, 3900000.0, "One of the fastest production cars in the world, powered by a quad-turbocharged W16 engine. Designed for extreme speed and luxury, representing the pinnacle of Bugatti engineering.", "HYPER", 1, 1600, 2.4, 490.0),

        ("KOENIGSEGG", "Jesko Absolut", 2024, 4000000.0, "Engineered to break speed records, this hypercar focuses on aerodynamic efficiency and extreme power output, aiming to surpass 500 km/h under optimal conditions.", "HYPER", 1, 1600, 2.5, 500.0),

        ("RIMAC", "Nevera", 2023, 2400000.0, "A revolutionary all-electric hypercar delivering record-breaking acceleration and power. Known as one of the most powerful production cars ever built.", "HYPER", 1, 1914, 1.85, 412.0),

        ("PAGANI", "Huayra R", 2022, 3100000.0, "A track-only masterpiece featuring a naturally aspirated V12 engine, lightweight construction, and extreme aerodynamic performance, built for pure driving emotion.", "HYPER", 1, 850, 2.8, 350.0),

        ("MCLAREN", "P1", 2015, 1800000.0, "McLaren’s first hybrid hypercar combining electric power with a twin-turbo V8, delivering exceptional performance and cutting-edge engineering.", "HYPER", 1, 916, 2.8, 350.0),

        ("FERRARI", "SF90 Stradale", 2023, 750000.0, "Ferrari’s first plug-in hybrid production car, combining a twin-turbo V8 with three electric motors for unmatched performance and responsiveness.", "HYPER", 1, 986, 2.5, 340.0),

        ("LAMBORGHINI", "Aventador SVJ", 2021, 650000.0, "A naturally aspirated V12 monster, famous for setting Nürburgring lap records and delivering raw Lamborghini performance and aggressive styling.", "HYPER", 1, 770, 2.8, 350.0),

        ("ASTON MARTIN", "Valkyrie", 2024, 3500000.0, "A hypercar inspired directly by Formula 1 technology, developed with Red Bull Racing, delivering extreme aerodynamics and lightweight construction.", "HYPER", 1, 1160, 2.5, 350.0),

        ("MCLAREN", "Speedtail", 2022, 2300000.0, "The fastest McLaren ever built, featuring a unique three-seat layout and optimized aerodynamics to achieve over 400 km/h.", "HYPER", 1, 1055, 2.9, 403.0),

        ("FERRARI", "LaFerrari Aperta", 2017, 3200000.0, "An open-top version of the legendary LaFerrari, combining hybrid technology with breathtaking design and extreme exclusivity.", "HYPER", 1, 963, 2.5, 350.0),

        ("FERRARI", "FXX-K Evo", 2020, 2700000.0, "A track-only evolution of the LaFerrari, featuring enhanced aerodynamics and performance, built exclusively for Ferrari’s Corse Clienti program.", "HYPER", 1, 1050, 2.5, 350.0),

        ("LAMBORGHINI", "Essenza SCV12", 2023, 2600000.0, "A track-only hypercar powered by the most powerful naturally aspirated V12 Lamborghini has ever built, designed for pure racing experience.", "HYPER", 1, 830, 2.6, 350.0),

        ("BUGATTI", "Centodieci", 2022, 9000000.0, "A tribute to the legendary EB110, limited to only 10 units, combining modern performance with classic Bugatti design heritage.", "HYPER", 1, 1600, 2.4, 380.0),

        ("PAGANI", "Zonda R", 2010, 1900000.0, "A track-focused evolution of the iconic Zonda, featuring extreme performance, lightweight materials, and a naturally aspirated AMG V12 engine.", "HYPER", 1, 750, 2.7, 370.0),

        ("KOENIGSEGG", "Agera RS", 2018, 2500000.0, "A former world speed record holder, combining lightweight design, immense power, and advanced aerodynamics for ultimate performance.", "HYPER", 1, 1341, 2.8, 457.0),

        ("HENNESSEY", "Venom F5", 2023, 2100000.0, "An American-built hypercar designed to exceed 500 km/h, featuring a twin-turbo V8 engine and extreme lightweight construction.", "HYPER", 1, 1817, 2.6, 500.0),

        ("PININFARINA", "Battista", 2023, 2500000.0, "An all-electric Italian hypercar combining luxury design with extreme performance, delivering nearly 1900 horsepower.", "HYPER", 1, 1900, 1.9, 350.0),

        ("GORDON MURRAY", "T.50", 2024, 3000000.0, "A spiritual successor to the McLaren F1, featuring a high-revving V12 engine and innovative fan-assisted aerodynamics.", "HYPER", 1, 654, 3.0, 370.0),

        ("CZINGER", "21C", 2024, 1700000.0, "A futuristic hypercar built using 3D printing and AI-driven design, delivering extreme performance and unique engineering solutions.", "HYPER", 1, 1250, 1.9, 400.0),

        ("SSC", "Tuatara", 2023, 1900000.0, "An American hypercar focused on achieving extreme top speeds, featuring a twin-turbo V8 and record-breaking performance ambitions.", "HYPER", 1, 1750, 2.5, 475.0);
    """.trimIndent())
}
fun seedDatabase(db: SupportSQLiteDatabase) {
    SEED_CATEGORIES.forEach {
        db.execSQL("INSERT INTO category (name) VALUES (?)", arrayOf(it))
    }
    SEED_BRANDS.forEach {
        db.execSQL("INSERT INTO brand (name) VALUES (?)", arrayOf(it))
    }
    seedTopCars(db)
}

