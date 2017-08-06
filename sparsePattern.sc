SparsePattern{

	var original, <patterns, <subpatterns, <appendedPatterns;

	*new{|original|
		^super.newCopyArgs(original)
	}

	makeSparse{|startFirst=false, reorder=false|
		var beatsum, order, copy, rotate = 0, arr;

		arr = original.collect(_.sum)
			.collect({|count, i| (count:count,index:i) })
			.sort({|a, b| a['count'] < b['count'] })
//			.sort({|a, b| a['count'] > b['count'] })
			.collect(_.index);

		order = Pseq(arr, 1).asStream;

		copy = Array.fill(original.first.size, {
			(0 ! original.size)
		});

		beatsum = original.flop.collect(_.sum);

//		(1..beatsum.maxItem).do({|num|
		(beatsum.maxItem..1).do({|num|
			beatsum.selectIndices({|sum| sum == num }).do({|ind|
				var slot;
				slot = order.next;
				if (slot.notNil) {
					copy[ind][slot] = 1;
				}
			})
		});

		if (startFirst) { rotate = copy.selectIndices({|a| a.first == 1 }).first };

		patterns = Array.newClear(copy.flop.size);

		copy.flop.do({|seq, i|
			patterns[arr[i]] = seq.rotate(rotate.neg);
		});

		if (reorder) { this.reorder }

	}

	reorder{
		patterns.do({|pat, i|
			var j;
			if (original[i][pat.indexOf(1)] == 0) {
				j = original.selectIndices({|it, itind|
					(it[pat.indexOf(1)] == 1).and(itind > i)
				}).first;
				if (j.notNil) {
					patterns.swap(i, j)
				}
			}
		});
	}

	makeSubPatterns{|numPatterns=3|
		subpatterns = Array();
		numPatterns.do({|i|
			subpatterns = subpatterns.add(
				SparsePattern(original - ([patterns] ++ subpatterns).sum).makeSparse.patterns
			);
		});
	}

	appendSubPatterns{
		appendedPatterns = patterns.copy;
		subpatterns.do({|subpat|
			subpat.do({|row, i|
				appendedPatterns[i] = appendedPatterns[i] ++ row
			})
		})
	}

}