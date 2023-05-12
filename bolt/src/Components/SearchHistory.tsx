import getSearchHistory from "../utils/getSearchHistory";
import { AiFillCloseCircle } from "react-icons/ai";
import removeSearchQueryFromHistory from "../utils/removeSearchQueryFromHistory";
import { useState } from "react";
import useSearchParamQuery from "../hooks/useSearchParamQuery";
type Props = {
	filter: string;
	setText: React.Dispatch<React.SetStateAction<string>>;
	inputRef: React.RefObject<HTMLInputElement>;
};

export default function SearchHistory({ filter, setText, inputRef }: Props) {
	const [searchHistory, setSearchHistory] = useState(getSearchHistory());
	const [query, isSeeingResults, setQuery] = useSearchParamQuery();
	const sortedHistory = Object.entries(searchHistory)
		.sort((a, b) => b[1] - a[1])
		.slice(0, 7)
		.map(([query]) => query);
	if (sortedHistory.length != 0)
		return (
			<ul
				className={`transition-all flex flex-col py-2 my-11 mx-auto border-2 rounded-lg border-primary bg-overlay text-stone-500 ${
					isSeeingResults() ? "w-10/12" : "w-6/12"
				}`}>
				{sortedHistory
					.filter((query) =>
						query
							.toLowerCase()
							.startsWith(filter.trim().toLowerCase())
					)
					.map((query, index) => (
						<li
							key={index}
							className=" justify-between px-6 mx-2 flex-grow hover:bg-primary rounded-full transition-all flex flex-row items-center ">
							<span
								className="py-2 flex-grow  cursor-pointer rounded-full  transition-all flex flex-row items-center "
								onClick={() => {
									setText(query);
									inputRef.current?.focus();
								}}>
								{query}
							</span>

							<span
								className=" cursor-pointer hover:text-red-900"
								onClick={() => {
									removeSearchQueryFromHistory(query);
									const newSearch = { ...searchHistory };
									delete newSearch[query];
									setSearchHistory(newSearch);
								}}>
								<AiFillCloseCircle />
							</span>
						</li>
					))}
			</ul>
		);
	else return <></>;
}
