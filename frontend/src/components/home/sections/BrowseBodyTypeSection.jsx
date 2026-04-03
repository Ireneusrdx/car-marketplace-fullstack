import { Car, CarFront, Truck } from "lucide-react";
import { Link } from "react-router-dom";

const bodyTypes = [
  { name: "Sedan", slug: "SEDAN" },
  { name: "SUV", slug: "SUV" },
  { name: "Hatchback", slug: "HATCHBACK" },
  { name: "Coupe", slug: "COUPE" },
  { name: "Truck", slug: "TRUCK" },
  { name: "Van", slug: "VAN" }
];

const icons = [Car, CarFront, Car, CarFront, Truck, CarFront];

export default function BrowseBodyTypeSection() {
  return (
    <section className="bg-white px-6 py-20">
      <div className="mx-auto w-full max-w-content text-center">
        <h2 className="font-montserrat text-4xl font-bold text-gray-900">FIND BY TYPE</h2>

        <div className="mt-10 grid gap-4 md:grid-cols-3 xl:grid-cols-6">
          {bodyTypes.map((type, index) => {
            const Icon = icons[index] || Car;
            return (
              <Link
                key={type.name}
                to={`/listings?bodyType=${type.slug}`}
                className="group cursor-pointer rounded-3xl border border-transparent bg-gray-50 p-8 text-center transition-all duration-300 hover:border-blue/20 hover:bg-blue/5"
              >
                <Icon className="mx-auto text-gray-300 transition-colors group-hover:text-blue" size={52} strokeWidth={1.5} />
                <p className="mt-4 font-montserrat text-lg font-bold text-gray-900">{type.name}</p>
              </Link>
            );
          })}
        </div>
      </div>
    </section>
  );
}
