import { useEffect, useState, useRef } from "react";
import ChangeColors from "./Components/ChangeColors";
import Search from "./Components/Search";
import { applyTheme } from "./themes/utils";
import dark from "./themes/dark";
import SearchHistory from "./Components/SearchHistory";
import addSearchQueryToHistory from "./utils/addSearchQueryToHistory";
import Logo from "./Components/Logo";
import useSearchParamQuery from "./hooks/useSearchParamQuery";
import ResultsList from "./Components/ResultsList";

export type historyItem = {
	body: string;
	visited: number;
};
export default function App() {
	const [query, isSeeingResults, setQuery] = useSearchParamQuery();

	const [searchHistoryList, setSearchHistoryList] = useState<historyItem[]>(
		[]
	);
	const [isSearching, setIsSearching] = useState(false);
	const [txt, setText] = useState("");
	const [isFocused, setIsFocused] = useState(false);
	const [isMouseInside, setIsMouseInside] = useState(false);

	const fetchingHistory = async () => {
		try {
			const res = await fetch("http://localhost:8080/search/history");
			const data: historyItem[] = await res.json();
			console.log(data);
			setSearchHistoryList(data);
		} catch (err) {
			console.log(err);
		}
	};

	const inputRef = useRef<HTMLInputElement>(null);
	useEffect(() => {
		applyTheme(dark);
		fetchingHistory();
	}, []);

	useEffect(() => {
		if (isSeeingResults()) {
			addSearchQueryToHistory(query);
		}
		fetchingHistory();
	}, [query]);

	return (
		<div className={` mx-auto bg-primary w-full min-h-screen`}>
			<div
				className={`w-4/5 mx-auto py-5 flex transition-all flex-wrap ${
					isSeeingResults()
						? "flex-row-reverse justify-between "
						: "flex-col"
				}`}>
				<ChangeColors />
				<div
					className={`flex   gap-20 flex-grow transition-all ${
						isSeeingResults() ? "fles-row" : "flex-col py-32"
					}`}>
					<Logo />
					<div className="flex  relative flex-col w-full justify-between">
						<Search
							txt={txt}
							setText={setText}
							setIsSearching={setIsSearching}
							isSearching={isSearching}
							setIsFocused={setIsFocused}
							inputRef={inputRef}
						/>
						{/* TODO: fix it */}
						{(isMouseInside || isFocused) && (
							<SearchHistory
								filter={txt}
								setText={setText}
								inputRef={inputRef}
								setIsMouseInside={setIsMouseInside}
								searchHistoryList={searchHistoryList}
							/>
						)}
					</div>
				</div>
				{isSeeingResults() && <ResultsList />}
			</div>
		</div>
	);
}
