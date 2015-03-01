import java.io.*;
import java.util.*;

class PR{
	public Page[] getPageArray() throws IOException{
		BufferedReader b = new BufferedReader(new InputStreamReader(System.in));

		String counts = b.readLine();
		int nodeCount = Integer.parseInt(counts.split(" ")[0]);
		int edgeCount = Integer.parseInt(counts.split(" ")[1]);

		String[] edgesRep = new String[edgeCount];
		for(int i=0; i<edgeCount; i++){
			String edgeRep = b.readLine();
			edgesRep[i] = edgeRep;
		}
		
		Page[] pages = getFreshPageArray(nodeCount);
		pages = populatePages(pages, edgesRep);

		return pages;
	}

	private Page[] getFreshPageArray(int nodeCount){
		Page[] pages = new Page[nodeCount];

                for(int i=0; i<nodeCount; i++){
                        Page page = new Page();

			page.score = 1/(double)nodeCount;
			page.oldScore = page.score;

                        pages[i] = page;
                }

		return pages;
	}

	private Page[] populatePages(Page[] pages, String[] edgesRep){
		for(String edgeRep: edgesRep){
			int pageId1 = Integer.parseInt(edgeRep.split(" ")[0]);
			int pageId2 = Integer.parseInt(edgeRep.split(" ")[1]);

			Page page1 = pages[pageId1 - 1];
			Page page2 = pages[pageId2 - 1];

			page1.children += 1;
			page2.parentIds.add(pageId1 - 1);
		}

		return handleSinkPages(pages);
	}

	private Page[] handleSinkPages(Page[] pages){
		for(int i=0; i<pages.length; i++){
			if(pages[i].children == 0){
				pages[i].children = pages.length;
				
				for(int j=0; j<pages.length; j++){
					pages[j].parentIds.add(i);
				}
			}
		}

		return pages;
	}

	public Page[] updateScore(Page[] pages){
		for(Page page: pages){
			page.oldScore = page.score;
			
			double influenceScore = 0;
			for(Integer parentId: page.parentIds){
				Page parent = pages[parentId];
				influenceScore += parent.oldScore/parent.children;
			}
	
			page.score = 0.15/pages.length + 0.85*influenceScore;
		}

		return pages;
	}

	public Page[] runRandomWalk(Page[] pages){
		do{
			pages = updateScore(pages);
		} while(pageScoresDiff(pages) != 0);

		return pages;
	}

	public double pageScoresDiff(Page[] pages){
		double diff = 0;

		for(Page page: pages){
			diff += Math.abs(page.score - page.oldScore);
		}

		return diff;
	}

	public static void main(String args[]) throws IOException{
		PR pr = new PR();

		Page[] pages = pr.getPageArray();
 		pages = pr.runRandomWalk(pages);

		for(Page page: pages){
			System.out.println(page.score);
		}
	}
}

class Page{
	int children;
	List<Integer> parentIds;
	double score;
	double oldScore;

	Page(){
		parentIds = new ArrayList<Integer>();
		score = 0;
		oldScore = 0;
		children = 0;
	}
}
