let a ={
  type: 'expression',
  head: {
    head: 2,
    tail: [
      {
        op: '*',
        target: {
          head: {
            head: {
              variable: 'u'
            },
            tail: []
          },
          tail: [
            {
              op: '+',
              target: {
                head: 4,
                tail: [
                  {
                    op: '*',
                    target: {
                      head: {
                        head: 1,
                        tail: []
                      },
                      tail: [
                        {
                          op: '+',
                          target: {
                            head: 3,
                            tail: []
                          }
                        }
                      ]
                    }
                  },
                  {
                    op: '*',
                    target: 5
                  }
                ]
              }
            },
            {
              op: '+',
              target: {
                head: 1,
                tail: []
              }
            }
          ]
        }
      }
    ]
  },
  tail: [],
  location: {
    source: undefined,
    start: {
      offset: 0,
      line: 1,
      column: 1
    },
    end: {
      offset: 31,
      line: 1,
      column: 32
    }
  }
}
